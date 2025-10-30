# Documentation Reorganization Summary

This document summarizes the documentation structure reorganization completed on [date].

## Goals

1. **Separate end-user from developer documentation**
2. **Create single entry point (README.md)**
3. **Organize detailed guides in docs/ folder**
4. **Archive temporary development notes**

## New Structure

### End-User Documentation

**README.md** - Main entry point
- Quick start and installation
- Basic usage examples
- Feature overview
- Links to detailed documentation

**docs/installation.md** - Installation guide
- Plugin installation for pipeline developers
- System requirements
- Configuration setup

**docs/configuration.md** - Configuration reference
- YAML configuration format
- Set types (plain, named, sequential, mixed)
- Cross-modal broadcasting
- Complete examples

**docs/api.md** - API reference
- Channel.fromBIDS() method signatures
- Parameter documentation
- Output format specification
- Usage in workflows

**docs/examples.md** - Usage examples
- Basic examples (T1w, multi-echo fMRI, DWI)
- Advanced workflows (cross-modal, parallel tasks)
- Common use cases (field maps, QC)
- Best practices

### Developer Documentation

**docs/development.md** - Development guide
- Setup instructions
- Building the plugin
- Code style guidelines
- Contributing workflow

**docs/architecture.md** - Architecture guide
- Plugin architecture
- Component descriptions
- Design patterns
- Data flow diagrams

**docs/implementation.md** - Implementation roadmap
- Step-by-step implementation guide
- Phase-by-phase breakdown
- Reference code mappings
- Completion checklist

**docs/testing.md** - Testing guide
- Testing strategy
- Unit tests
- Integration tests
- Validation tests

### Archived Development Notes

**.dev-notes/** - Temporary/development files
- `OVERVIEW.txt` - Initial project overview
- `START_HERE.md` - Initial orientation guide
- `SCAFFOLDING_SUMMARY.md` - Scaffolding creation summary
- `GETTING_STARTED.md` - Original getting started
- `README-old.md` - Original README

## Documentation Map

```
nf-bids/
├── README.md                          # ← START HERE (end-users)
├── docs/
│   ├── installation.md                # Plugin installation
│   ├── configuration.md               # YAML configuration
│   ├── api.md                         # API reference
│   ├── examples.md                    # Usage examples
│   ├── development.md                 # ← Developer setup
│   ├── architecture.md                # Architecture guide
│   ├── implementation.md              # Implementation roadmap
│   └── testing.md                     # Testing guide
└── .dev-notes/
    ├── OVERVIEW.txt                   # Archived
    ├── START_HERE.md                  # Archived
    ├── SCAFFOLDING_SUMMARY.md         # Archived
    ├── GETTING_STARTED.md             # Archived
    └── README-old.md                  # Archived
```

## Documentation Flow

### For Pipeline Developers (End-Users)

1. **README.md** - Quick overview and installation
2. **docs/installation.md** - Detailed installation steps
3. **docs/configuration.md** - Configure BIDS channel factory
4. **docs/examples.md** - Copy/paste examples
5. **docs/api.md** - Reference when needed

### For Plugin Developers

1. **README.md** - Project overview
2. **docs/development.md** - Setup development environment
3. **docs/architecture.md** - Understand plugin structure
4. **docs/implementation.md** - Follow implementation steps
5. **docs/testing.md** - Write and run tests

## Cross-References

All documentation files include cross-references:

- **README.md** → links to all docs/
- **docs/installation.md** → references configuration.md, examples.md
- **docs/configuration.md** → references api.md, examples.md
- **docs/api.md** → references configuration.md, examples.md
- **docs/examples.md** → references api.md, configuration.md
- **docs/development.md** → references architecture.md, implementation.md, testing.md
- **docs/architecture.md** → references implementation.md
- **docs/implementation.md** → references architecture.md, development.md
- **docs/testing.md** → references development.md, implementation.md

## File Changes

### Moved Files

```bash
# Temporary/development files → .dev-notes/
OVERVIEW.txt                  → .dev-notes/OVERVIEW.txt
START_HERE.md                 → .dev-notes/START_HERE.md
SCAFFOLDING_SUMMARY.md        → .dev-notes/SCAFFOLDING_SUMMARY.md
GETTING_STARTED.md            → .dev-notes/GETTING_STARTED.md
README.md                     → .dev-notes/README-old.md

# Detailed guides → docs/
INSTALL.md                    → docs/installation.md
ARCHITECTURE.md               → docs/architecture.md
IMPLEMENTATION_GUIDE.md       → docs/implementation.md
```

### New Files

```bash
# Created
README.md                     # New end-user focused README
docs/configuration.md         # YAML configuration guide
docs/api.md                   # API reference
docs/examples.md              # Usage examples
docs/development.md           # Developer guide
docs/testing.md               # Testing guide
.dev-notes/                   # Archive directory
```

## Benefits

### For End-Users (Pipeline Developers)

✅ **Single entry point**: README.md with clear quick start
✅ **Focused content**: Only what's needed to use the plugin
✅ **Easy navigation**: Clear links to detailed guides
✅ **Practical examples**: Copy/paste workflows

### For Contributors (Plugin Developers)

✅ **Clear roadmap**: Step-by-step implementation guide
✅ **Architecture docs**: Understand design decisions
✅ **Testing guide**: Comprehensive test strategies
✅ **Separated concerns**: User docs don't clutter dev docs

### For Maintainers

✅ **Organized structure**: docs/ for production, .dev-notes/ for archive
✅ **Easy updates**: Each guide has single responsibility
✅ **Version control**: Clean git history of documentation changes

## Next Steps

1. ✅ Documentation structure reorganized
2. ⏭️ Implement plugin functionality (see docs/implementation.md)
3. ⏭️ Add more examples as use cases emerge
4. ⏭️ Update docs as implementation progresses
5. ⏭️ Generate API docs from source code (Groovydoc)

## Maintenance

### Updating Documentation

**When adding features:**
1. Update docs/api.md with new methods/parameters
2. Add examples to docs/examples.md
3. Update README.md if it affects quick start

**When changing architecture:**
1. Update docs/architecture.md
2. Update docs/implementation.md if roadmap changes
3. Update docs/development.md if setup changes

**When deprecating features:**
1. Mark as deprecated in docs/api.md
2. Update examples to show preferred approach
3. Add migration guide to docs/

### Documentation Review Checklist

- [ ] README.md is concise (< 200 lines)
- [ ] All docs/ files have cross-references
- [ ] Examples are tested and work
- [ ] API reference matches implementation
- [ ] No broken links
- [ ] Consistent terminology
- [ ] Code blocks have syntax highlighting
- [ ] All files have table of contents

## Resources

- **Nextflow Documentation Style**: https://www.nextflow.io/docs/latest/
- **BIDS Specification Docs**: https://bids-specification.readthedocs.io/
- **Markdown Guide**: https://www.markdownguide.org/

---

**Last Updated**: [Automatically generated during reorganization]
**Maintained By**: nf-bids development team
**Contact**: See main README.md for support channels
