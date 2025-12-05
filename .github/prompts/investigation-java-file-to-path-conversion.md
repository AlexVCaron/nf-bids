# Investigation: Java File to Nextflow Path Conversion Issue

**Date:** December 4, 2025  
**Status:** Investigation Complete  
**Priority:** HIGH - Blocks process input compatibility

## Problem Statement

The nf-bids plugin currently outputs `java.io.File` objects in channel data structures. These are **incompatible** with Nextflow process `path` inputs, causing runtime failures when users try to consume plugin outputs in downstream processes.

## Root Cause Analysis

### Issue Location

**File:** `src/main/groovy/nfneuro/channel/BidsHandler.groovy`  
**Method:** `buildFlattenedOutput()`  
**Lines:** 210-228

```groovy
Closure convertValue
convertValue = { Object val ->
    if (val == null) return null
    if (val instanceof File) return val           // ❌ Returns java.io.File
    if (val instanceof String) {
        File f = new File(val)                    // ❌ Creates java.io.File
        if (f.isAbsolute()) return f
        return new File(bidsParentDir ?: '', val) // ❌ Creates java.io.File
    }
    if (val instanceof List) {
        return (val as List).collect { convertValue.call(it) }
    }
    if (val instanceof Map) {
        Map nested = [:]
        (val as Map).each { k2, v2 -> nested[(String)k2] = convertValue.call(v2) }
        return nested
    }
    return val
}
```

### Why This Breaks

**Nextflow Documentation (from nextflow.io/docs/latest/process.html#input-files-path):**

> The `path` qualifier allows you to provide input files to the process execution context. Nextflow will stage the files into the process execution directory, and they can be accessed in the script by using the specified input name.
>
> Channel factories like `channel.fromPath` produce **file objects** [referring to `java.nio.file.Path`], but a `path` input can also accept a string literal path.

**Key Finding:** Nextflow processes expect `java.nio.file.Path` objects, NOT `java.io.File` objects.

### Nextflow Standard Library Types (from nextflow.io/docs/latest/reference/stdlib-types.html#path)

The `Path` type in Nextflow:
- Backed by `java.nio.file.Path` and Groovy Path extensions
- Handles hierarchical paths: local files, directories, HTTP/FTP URLs, object storage (S3, Azure, GCS)
- Created via `file()` function: `def hello = file('hello.txt')`
- Supports methods like `.exists()`, `.text`, `.name`, `.parent`, etc.

**Critical distinction:**
- `java.io.File` - Legacy Java file API (JDK 1.0)
- `java.nio.file.Path` - Modern NIO.2 API (JDK 7+), used by Nextflow

## Data Flow Through Plugin

### 1. BidsFile Model (Correct)

**File:** `src/main/groovy/nfneuro/model/BidsFile.groovy`

```groovy
class BidsFile {
    String path  // ✅ Stores as String
    
    // Uses Path for operations
    String getFilename() {
        return new File(path).name  // ⚠️ Temporary File for name extraction
    }
    
    String relativeTo(String basePath) {
        def base = Paths.get(basePath).toAbsolute()      // ✅ Uses Path
        def filePath = Paths.get(path).toAbsolutePath()  // ✅ Uses Path
        return base.relativize(filePath).toString()
    }
}
```

**Status:** ✅ Internally uses `java.nio.file.Paths` correctly for operations

### 2. BidsParser (Correct)

**File:** `src/main/groovy/nfneuro/parser/BidsParser.groovy`

Creates `BidsFile` objects with String paths - no File objects involved. ✅

### 3. Set Handlers (Neutral)

**Files:** 
- `src/main/groovy/nfneuro/grouping/PlainSetHandler.groovy`
- `src/main/groovy/nfneuro/grouping/NamedSetHandler.groovy`
- `src/main/groovy/nfneuro/grouping/SequentialSetHandler.groovy`
- `src/main/groovy/nfneuro/grouping/MixedSetHandler.groovy`

Set handlers store `BidsFile.path` (String) in output maps. No File objects created here. ✅

### 4. BidsHandler.buildFlattenedOutput() (PROBLEM)

**File:** `src/main/groovy/nfneuro/channel/BidsHandler.groovy`  
**Lines:** 210-228

This is where String paths are **incorrectly** converted to `java.io.File` objects:

```groovy
if (val instanceof String) {
    File f = new File(val)                    // ❌ PROBLEM HERE
    if (f.isAbsolute()) return f
    return new File(bidsParentDir ?: '', val) // ❌ AND HERE
}
```

**Impact:** All file paths in the output channel become `java.io.File` objects instead of remaining as Strings (which Nextflow can handle) or being converted to `java.nio.file.Path` objects.

## Evidence from Nextflow Codebase

### From Nextflow Source (github.com/nextflow-io/nextflow)

**File:** `modules/nextflow/src/main/groovy/nextflow/processor/TaskInputResolver.groovy`

```groovy
protected List<FileHolder> normalizeInputToFiles( Object obj, int count, boolean coerceToPath ) {
    Collection allItems = obj instanceof Collection ? obj : [obj]
    def files = new ArrayBag<FileHolder>(len)
    
    for( def item : allItems ) {
        if( item instanceof Path || coerceToPath ) {
            final path = resolvePath(item)      // ✅ Expects Path
            final target = executor.isForeignFile(path) ? foreignFiles.addToForeign(path) : path
            final holder = new FileHolder(path, target)
            files << holder
        }
        else {
            files << normalizeInputToFile(item, "input.${++count}")
        }
    }
    
    return files
}
```

**Key observation:** Nextflow's task input resolver expects `java.nio.file.Path` objects, not `java.io.File`.

**File:** `modules/nf-commons/src/main/nextflow/file/FileHelper.groovy`

```groovy
static Path toPath(value) {
    if( value==null )
        return null

    Path result = null
    if( value instanceof String || value instanceof GString ) {
        result = asPath(value.toString())
    }
    else if( value instanceof Path ) {
        result = (Path)value
    }
    else {
        throw new IllegalArgumentException("Unexpected path value: '$value' [${value.getClass().getName()}]")
    }
    return result
}
```

**Key observation:** `FileHelper.toPath()` handles String and Path, but NOT `java.io.File`. This will throw an exception for File objects.

## Solution Architecture

### Option 1: Return Strings (Simplest)

**Approach:** Don't convert paths to File objects at all - leave them as Strings.

**Pros:**
- Minimal code changes
- Nextflow automatically converts String paths to Path objects
- Works with Nextflow's documented behavior

**Cons:**
- Strings don't provide file metadata (size, exists, etc.) in the channel
- Users would need to convert to Path in their process if they need file operations before staging

**Implementation:**
```groovy
convertValue = { Object val ->
    if (val == null) return null
    if (val instanceof String) {
        // Just return the string - Nextflow will convert to Path as needed
        return val  // ✅ Simple fix
    }
    if (val instanceof List) {
        return (val as List).collect { convertValue.call(it) }
    }
    if (val instanceof Map) {
        Map nested = [:]
        (val as Map).each { k2, v2 -> nested[(String)k2] = convertValue.call(v2) }
        return nested
    }
    return val
}
```

### Option 2: Convert to java.nio.file.Path (Recommended)

**Approach:** Convert String paths to `java.nio.file.Path` objects using `FileHelper.asPath()` or `Paths.get()`.

**Pros:**
- Provides rich Path API in channel data
- Fully compatible with Nextflow process inputs
- Aligns with Nextflow's type system
- Users can call `.exists()`, `.text`, `.size()`, etc. on channel items

**Cons:**
- Slightly more complex implementation
- Need to handle both absolute and relative paths correctly
- Must import `java.nio.file.Path` and `java.nio.file.Paths`

**Implementation:**
```groovy
import java.nio.file.Path
import java.nio.file.Paths

convertValue = { Object val ->
    if (val == null) return null
    if (val instanceof Path) return val  // ✅ Already a Path
    if (val instanceof String) {
        // Convert to java.nio.file.Path
        String pathStr = val
        Path result = Paths.get(pathStr)
        
        // Make relative paths absolute using bidsParentDir
        if (!result.isAbsolute() && bidsParentDir) {
            result = Paths.get(bidsParentDir).resolve(pathStr)
        }
        
        return result  // ✅ Returns java.nio.file.Path
    }
    if (val instanceof List) {
        return (val as List).collect { convertValue.call(it) }
    }
    if (val instanceof Map) {
        Map nested = [:]
        (val as Map).each { k2, v2 -> nested[(String)k2] = convertValue.call(v2) }
        return nested
    }
    return val
}
```

### Option 3: Use FileHelper.asPath() (Most Robust)

**Approach:** Use Nextflow's built-in `FileHelper.asPath()` which handles all path schemes (local, S3, GCS, Azure, etc.)

**Pros:**
- Handles remote file systems automatically (S3, GCS, Azure Blob)
- Consistent with how Nextflow handles paths internally
- Robust error handling for invalid paths
- Auto-loads required plugins (nf-amazon, nf-google, nf-azure)

**Cons:**
- Requires importing Nextflow's FileHelper
- Plugin must be aware of Nextflow's file system abstraction

**Implementation:**
```groovy
import nextflow.file.FileHelper
import java.nio.file.Path

convertValue = { Object val ->
    if (val == null) return null
    if (val instanceof Path) return val  // ✅ Already a Path
    if (val instanceof String) {
        String pathStr = val
        
        // Use FileHelper for robust path handling
        Path result = pathStr.contains('://') || pathStr.startsWith('/')
            ? FileHelper.asPath(pathStr)
            : FileHelper.asPath(Paths.get(bidsParentDir ?: '', pathStr).toString())
        
        return result  // ✅ Returns java.nio.file.Path (with potential remote FS support)
    }
    if (val instanceof List) {
        return (val as List).collect { convertValue.call(it) }
    }
    if (val instanceof Map) {
        Map nested = [:]
        (val as Map).each { k2, v2 -> nested[(String)k2] = convertValue.call(v2) }
        return nested
    }
    return val
}
```

## Recommended Solution

**Use Option 3: FileHelper.asPath()** with the following rationale:

1. **Future-proof:** Works with local and remote file systems
2. **Consistent:** Uses the same path handling as other Nextflow operations
3. **Plugin-aware:** Automatically loads cloud storage plugins when needed
4. **Robust:** Handles edge cases (URIs, absolute paths, relative paths)
5. **Type-safe:** Returns `java.nio.file.Path` that Nextflow processes expect

## Implementation Plan

### Phase 1: Update BidsHandler.convertValue()

**File:** `src/main/groovy/nfneuro/channel/BidsHandler.groovy`

1. Add imports:
```groovy
import nextflow.file.FileHelper
import java.nio.file.Path
import java.nio.file.Paths
```

2. Replace lines 210-228 with Option 3 implementation above

### Phase 2: Remove File References

**Search for:** `java.io.File` usage in other files

Current findings:
- `BidsFile.groovy` - Uses `new File(path).name` for name extraction only - **OK to keep**
- `BidsConfigLoader.groovy` - Uses `new File(configPath)` - **Consider updating**
- `BidsValidator.groovy` - Uses `new File()` for existence checks - **Consider updating**
- `LibBidsShWrapper.groovy` - Multiple File usages - **Review individually**

### Phase 3: Update Tests

1. Verify output channel contains Path objects
2. Test that processes can consume Path inputs
3. Test with absolute and relative paths
4. Test with remote paths (S3, GCS, Azure) if applicable

### Phase 4: Documentation

1. Update `docs/architecture.md` - document Path usage
2. Update `README.md` - show example process consumption
3. Add code comments explaining Path choice over File

## Testing Strategy

### Test 1: Basic Path Conversion
```groovy
def result = buildFlattenedOutput(channelData)
assert result.T1w instanceof Path
assert result.T1w.class.name == 'sun.nio.fs.UnixPath'  // or similar
```

### Test 2: Process Compatibility
```nextflow
process consume_bids {
    input:
    path t1w  // Should work now
    
    script:
    """
    echo "T1w file: ${t1w}"
    ls -lh ${t1w}
    """
}

workflow {
    channel.fromBIDS('dataset', 'config.yaml')
        | map { it.T1w }
        | consume_bids
}
```

### Test 3: Relative Path Resolution
```groovy
// Given bidsParentDir = '/data/bids'
// And path = 'sub-01/anat/sub-01_T1w.nii.gz'
// Result should be absolute: '/data/bids/sub-01/anat/sub-01_T1w.nii.gz'

def result = FileHelper.asPath(Paths.get(bidsParentDir, relativePath).toString())
assert result.isAbsolute()
```

## Migration Impact

### Breaking Changes

**Yes - this is a breaking change for users who:**
1. Expect `java.io.File` objects in channel data
2. Call File-specific methods (e.g., `.listFiles()`)

**Mitigation:**
- Document the change in CHANGELOG.md
- Provide migration guide showing how to use Path API
- Path API is richer anyway, so this is an improvement

### Backward Compatibility

**Option:** Add a compatibility flag for one release:
```groovy
// In plugin config
bids {
    legacyFileObjects = false  // New default, use Path
    // legacyFileObjects = true   // For backward compat
}
```

**Recommendation:** Don't provide backward compat - this is beta software (v0.1.0-beta.7) and the fix is necessary for core functionality.

## Related Nextflow Documentation

1. **Process Inputs:** https://www.nextflow.io/docs/latest/process.html#input-files-path
2. **Standard Library Types:** https://www.nextflow.io/docs/latest/reference/stdlib-types.html#path
3. **Working with Files:** https://www.nextflow.io/docs/latest/working-with-files.html
4. **Nextflow Gradle Plugin:** https://www.nextflow.io/docs/latest/guides/gradle-plugin.html

## Action Items

- [ ] Implement Option 3 (FileHelper.asPath) in BidsHandler
- [ ] Review and update other File usages if needed
- [ ] Add unit tests for Path conversion
- [ ] Add integration test with process consumption
- [ ] Update documentation
- [ ] Update CHANGELOG.md with breaking change notice
- [ ] Test with cloud storage paths (S3, GCS, Azure)

## Questions for Discussion

1. Should we support both File and Path output via configuration flag?
   - **Recommendation:** No, enforce Path for consistency
   
2. Should we update all File usages in the plugin or just the output?
   - **Recommendation:** Update incrementally, prioritize output first
   
3. Do we need to handle Path serialization for resume functionality?
   - **Note:** Nextflow handles Path serialization automatically via Kryo (see `PathSerializer` in Nextflow source)

---

**Next Steps:** Implement Phase 1 and create a test workflow to validate the fix.
