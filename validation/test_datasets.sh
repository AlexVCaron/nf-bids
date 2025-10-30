#!/bin/bash
# Comprehensive dataset testing for nf-bids plugin

# Usage function
usage() {
    cat << EOF
Usage: $0 [OPTIONS] [DATASETS...]

Test nf-bids plugin with BIDS datasets.

OPTIONS:
    -h, --help          Show this help message
    -v, --verbose       Show full Nextflow output for each test
    -e, --extended      Show extended statistics with sub-item breakdowns
    -c, --custom        Run only custom datasets
    -b, --bids          Run only BIDS examples datasets
    -l, --list          List all available datasets
    
DATASETS:
    Specific dataset names to test (e.g., ds-dwi qmri_irt1)
    If not specified, all datasets will be tested.

EXAMPLES:
    $0                          # Run all datasets
    $0 -c                       # Run only custom datasets
    $0 ds-dwi qmri_irt1        # Run specific datasets
    $0 -v qmri_sa2rage         # Run with verbose output
    $0 -e ds-mtsat             # Run with extended sub-item details
    $0 -l                       # List available datasets

EOF
    exit 0
}

# Don't exit on error - we want to run all tests
set +e

PLUGIN_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BIDS2NF_ROOT="$(cd "${PLUGIN_DIR}/../.." && pwd)"
TEST_DATA="${BIDS2NF_ROOT}/tests/data"

# Parse arguments
VERBOSE=false
EXTENDED_STATS=false
CUSTOM_ONLY=false
BIDS_ONLY=false
LIST_ONLY=false
SPECIFIC_DATASETS=()

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            usage
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -e|--extended)
            EXTENDED_STATS=true
            shift
            ;;
        -c|--custom)
            CUSTOM_ONLY=true
            shift
            ;;
        -b|--bids)
            BIDS_ONLY=true
            shift
            ;;
        -l|--list)
            LIST_ONLY=true
            shift
            ;;
        -*)
            echo "Unknown option: $1"
            echo "Use -h or --help for usage information"
            exit 1
            ;;
        *)
            SPECIFIC_DATASETS+=("$1")
            shift
            ;;
    esac
done

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PASSED=0
FAILED=0
SKIPPED=0

# Check if we should run a specific dataset
should_run_dataset() {
    local name=$1
    local category=$2  # "custom" or "bids"
    
    # If listing, always include
    if [ "$LIST_ONLY" = true ]; then
        return 0
    fi
    
    # If specific datasets requested, check if this is one
    if [ ${#SPECIFIC_DATASETS[@]} -gt 0 ]; then
        for ds in "${SPECIFIC_DATASETS[@]}"; do
            if [ "$ds" = "$name" ]; then
                return 0
            fi
        done
        return 1
    fi
    
    # Category filters
    if [ "$CUSTOM_ONLY" = true ] && [ "$category" != "custom" ]; then
        return 1
    fi
    
    if [ "$BIDS_ONLY" = true ] && [ "$category" != "bids" ]; then
        return 1
    fi
    
    return 0
}

# Test function
test_dataset() {
    local name=$1
    local dataset=$2
    local config=$3
    local category=$4  # "custom" or "bids"
    
    # Check if we should run this dataset
    if ! should_run_dataset "$name" "$category"; then
        return
    fi
    
    # If listing only, just print and return
    if [ "$LIST_ONLY" = true ]; then
        echo -e "  ${BLUE}${name}${NC} (${category})"
        return
    fi
    
    echo -n "Testing ${name}... "
    
    if [ ! -d "${dataset}" ]; then
        echo -e "${YELLOW}SKIPPED${NC} (dataset not found)"
        ((SKIPPED++))
        return
    fi
    
    if [ ! -f "${config}" ]; then
        echo -e "${YELLOW}SKIPPED${NC} (config not found)"
        ((SKIPPED++))
        return
    fi
    
    # Run test and capture output
    local output
    if [ "$VERBOSE" = true ]; then
        echo ""  # New line before verbose output
        nextflow run "${PLUGIN_DIR}/validation/main.nf" \
            --bids_dir "${dataset}" \
            --config "${config}" \
            --libbids_sh "${BIDS2NF_ROOT}/libBIDS.sh/libBIDS.sh" \
            2>&1
        local exit_code=$?
        output=$(echo "")  # Empty for verbose mode
    else
        output=$(nextflow run "${PLUGIN_DIR}/validation/main.nf" \
            --bids_dir "${dataset}" \
            --config "${config}" \
            --libbids_sh "${BIDS2NF_ROOT}/libBIDS.sh/libBIDS.sh" \
            2>&1)
        local exit_code=$?
    fi
    
    # Check if completed successfully
    if echo "${output}" | grep -q "Pipeline completed" || echo "${output}" | grep -q "ITEM_STATS"; then
        echo -e "${GREEN}PASSED${NC}"
        
        # Extract basic statistics
        local num_files=$(find "${dataset}" -type f \( -name "*.nii" -o -name "*.nii.gz" -o -name "*.json" \) 2>/dev/null | wc -l)
        local num_items=$(echo "${output}" | grep -c "ITEM_STATS:" 2>/dev/null || echo "0")
        
        # Build entity summary from first item for header stats
        local entities_info=""
        if [ "${num_items}" -gt 0 ]; then
            # Extract unique entity values across all items
            local subjects=$(echo "${output}" | grep "ITEM_STATS:" | grep -oP '"sub":"[^"]+' | cut -d'"' -f4 | sort -u | tr '\n' ',' | sed 's/,$//')
            local sessions=$(echo "${output}" | grep "ITEM_STATS:" | grep -oP '"ses":"[^"]+' | cut -d'"' -f4 | grep -v "NA" | sort -u | tr '\n' ',' | sed 's/,$//')
            local runs=$(echo "${output}" | grep "ITEM_STATS:" | grep -oP '"run":"[^"]+' | cut -d'"' -f4 | grep -v "NA" | sort -u | tr '\n' ',' | sed 's/,$//')
            
            local num_subjects=$(echo "${subjects}" | awk -F',' '{print NF}')
            local num_sessions=$(echo "${sessions}" | awk -F',' '{print NF}')
            local num_runs=$(echo "${runs}" | awk -F',' '{print NF}')
            
            # Build compact entity summary
            local entity_parts=()
            [ "${num_subjects}" -gt 0 ] && [ -n "${subjects}" ] && {
                if [ "${num_subjects}" -le 5 ]; then
                    entity_parts+=("${num_subjects} subject$([ ${num_subjects} -gt 1 ] && echo 's' || echo '') (${subjects})")
                else
                    local first=$(echo "${subjects}" | cut -d',' -f1-3)
                    local last=$(echo "${subjects}" | rev | cut -d',' -f1-2 | rev)
                    entity_parts+=("${num_subjects} subjects (${first},...,${last})")
                fi
            }
            [ "${num_sessions}" -gt 0 ] && [ -n "${sessions}" ] && {
                if [ "${num_sessions}" -le 5 ]; then
                    entity_parts+=("${num_sessions} session$([ ${num_sessions} -gt 1 ] && echo 's' || echo '') (${sessions})")
                else
                    local first=$(echo "${sessions}" | cut -d',' -f1-3)
                    local last=$(echo "${sessions}" | rev | cut -d',' -f1-2 | rev)
                    entity_parts+=("${num_sessions} sessions (${first},...,${last})")
                fi
            }
            [ "${num_runs}" -gt 0 ] && [ -n "${runs}" ] && {
                if [ "${num_runs}" -le 5 ]; then
                    entity_parts+=("${num_runs} run$([ ${num_runs} -gt 1 ] && echo 's' || echo '') (${runs})")
                else
                    local first=$(echo "${runs}" | cut -d',' -f1-3)
                    local last=$(echo "${runs}" | rev | cut -d',' -f1-2 | rev)
                    entity_parts+=("${num_runs} runs (${first},...,${last})")
                fi
            }
            
            [ ${#entity_parts[@]} -gt 0 ] && entities_info=" ${entity_parts[*]}"
        fi
        
        # Display header summary
        if [ "${num_items}" -gt 0 ]; then
            echo "    📊 Files: ${num_files} | Items: ${num_items} |${entities_info}"
        else
            echo "    📊 Files: ${num_files} | ⚠️  No items emitted"
        fi
        
        # Parse and display detailed per-item statistics
        if [ "${num_items}" -gt 0 ]; then
            local item_num=0
            echo "${output}" | grep "ITEM_STATS:" | while IFS= read -r line; do
                ((item_num++))
                local json_data=$(echo "${line}" | sed 's/.*ITEM_STATS: //')
                
                # Extract key information using jq
                local set_type=$(echo "${json_data}" | jq -r '.type')
                local total_files=$(echo "${json_data}" | jq -r '.totalFiles')
                local suffixes=$(echo "${json_data}" | jq -r '.suffixes | join(",")')
                
                # Extract loop entities for this item
                local entity_str=""
                local sub=$(echo "${json_data}" | jq -r '.entities.sub')
                local ses=$(echo "${json_data}" | jq -r '.entities.ses')
                local run=$(echo "${json_data}" | jq -r '.entities.run')
                
                [ -n "${sub}" ] && [ "${sub}" != "NA" ] && [ "${sub}" != "null" ] && entity_str="${entity_str}${sub}"
                [ -n "${ses}" ] && [ "${ses}" != "NA" ] && [ "${ses}" != "null" ] && entity_str="${entity_str}_${ses}"
                [ -n "${run}" ] && [ "${run}" != "NA" ] && [ "${run}" != "null" ] && entity_str="${entity_str}_${run}"
                [ -z "${entity_str}" ] && entity_str="(no loop entities)"
                
                # Format based on set type
                echo -n "        Item ${item_num} [${entity_str}]: "
                
                case "${set_type}" in
                    plain)
                        echo "${suffixes} (${total_files} files)"
                        if [ "$EXTENDED_STATS" = true ]; then
                            # Show breakdown by suffix
                            echo "${json_data}" | jq -r '.details[] | "            ├─ \(.suffix): \(.files) files"'
                        fi
                        ;;
                    named)
                        # Extract group information from details
                        local groups=$(echo "${json_data}" | jq -r '[.details[].groups[].name] | join(",")')
                        if [ -n "${groups}" ] && [ "${groups}" != "" ]; then
                            local num_groups=$(echo "${groups}" | awk -F',' '{print NF}')
                            echo "${suffixes} → ${num_groups} groups [${groups}] (${total_files} files)"
                            if [ "$EXTENDED_STATS" = true ]; then
                                # Show breakdown by group
                                echo "${json_data}" | jq -r '.details[] | .suffix as $suf | .groups[] | "            ├─ \($suf):\(.name) → \(.files) files"'
                            fi
                        else
                            echo "${suffixes} named set (${total_files} files)"
                        fi
                        ;;
                    sequential)
                        # Extract sequential items count from first detail
                        local items_count=$(echo "${json_data}" | jq -r '.details[0].items // 0')
                        echo "${suffixes} → ${items_count} items (${total_files} files)"
                        if [ "$EXTENDED_STATS" = true ]; then
                            # Show breakdown by suffix with item distribution
                            echo "${json_data}" | jq -r '.details[] | "            ├─ \(.suffix): \(.items) items, \(.files) files"'
                        fi
                        ;;
                    hierarchical_sequential)
                        # Extract dimensions and items from first detail
                        local dimensions=$(echo "${json_data}" | jq -r '.details[0].dimensions // "?"')
                        local items=$(echo "${json_data}" | jq -r '.details[0].items // "?"')
                        echo "${suffixes} hierarchical (${dimensions}D, ${items} items, ${total_files} files)"
                        if [ "$EXTENDED_STATS" = true ]; then
                            # Show hierarchical structure breakdown with items at each level
                            echo "${json_data}" | jq -r '
                                .details[] | 
                                .suffix as $suf | 
                                if .hierarchy then
                                    ("            ├─ \($suf) hierarchy:"),
                                    (.hierarchy | to_entries | sort_by(.key) | .[] | 
                                        "              ├─ \(.key): " + 
                                        (if (.value | type) == "array" then
                                            (.value | map(
                                                if .items then "\(.name) (\(.items) items)"
                                                elif .files then "\(.name) (\(.files) files)"
                                                else .name
                                                end
                                            ) | join(", "))
                                        else
                                            (.value | tostring)
                                        end)
                                    )
                                else
                                    "            ├─ \($suf): \(.dimensions)D, \(.items) items, \(.files) files"
                                end
                            '
                        fi
                        ;;
                    mixed)
                        # Extract mixed set group information from details
                        local groups=$(echo "${json_data}" | jq -r '[.details[].groups[].name] | join(",")')
                        if [ -n "${groups}" ] && [ "${groups}" != "" ]; then
                            local num_groups=$(echo "${groups}" | awk -F',' '{print NF}')
                            echo "${suffixes} → ${num_groups} groups [${groups}] × sequential (${total_files} files)"
                            if [ "$EXTENDED_STATS" = true ]; then
                                # Show breakdown by group with sequential items
                                echo "${json_data}" | jq -r '
                                    .details[] | 
                                    .suffix as $suf | 
                                    .groups[] | 
                                    "            ├─ \($suf):\(.name) → \(.items) items, \(.files) files"
                                '
                            fi
                        else
                            echo "${suffixes} mixed set (${total_files} files)"
                        fi
                        ;;
                    *)
                        echo "${set_type} (${total_files} files)"
                        ;;
                esac
            done
        fi
        
        ((PASSED++))
    else
        echo -e "${RED}FAILED${NC}"
        local num_files=$(find "${dataset}" -type f \( -name "*.nii" -o -name "*.nii.gz" -o -name "*.json" \) 2>/dev/null | wc -l)
        echo "    📊 Files: ${num_files} | Error during execution"
        echo "Last 15 lines of output:"
        echo "${output}" | tail -15
        ((FAILED++))
    fi
}

# Print header
if [ "$LIST_ONLY" = true ]; then
    echo "========================================="
    echo "Available Datasets"
    echo "========================================="
    echo ""
    echo "Custom Datasets:"
else
    echo "========================================="
    echo "nf-bids Plugin - Dataset Testing"
    echo "========================================="
    if [ ${#SPECIFIC_DATASETS[@]} -gt 0 ]; then
        echo "Running specific datasets: ${SPECIFIC_DATASETS[*]}"
    elif [ "$CUSTOM_ONLY" = true ]; then
        echo "Running custom datasets only"
    elif [ "$BIDS_ONLY" = true ]; then
        echo "Running BIDS examples datasets only"
    else
        echo "Running all datasets"
    fi
    echo ""
fi

echo "=== Custom Datasets ==="
echo ""

# Test ds-dwi (already tested, baseline)
test_dataset "ds-dwi" \
    "${TEST_DATA}/custom/ds-dwi" \
    "${PLUGIN_DIR}/validation/configs/config_dwi.yaml" \
    "custom"

# Test ds-dwi2
test_dataset "ds-dwi2" \
    "${TEST_DATA}/custom/ds-dwi2" \
    "${PLUGIN_DIR}/validation/configs/config_dwi.yaml" \
    "custom"

# Test ds-dwi3
test_dataset "ds-dwi3" \
    "${TEST_DATA}/custom/ds-dwi3" \
    "${PLUGIN_DIR}/validation/configs/config_dwi.yaml" \
    "custom"

# Test ds-dwi4
test_dataset "ds-dwi4" \
    "${TEST_DATA}/custom/ds-dwi4" \
    "${PLUGIN_DIR}/validation/configs/config_dwi.yaml" \
    "custom"

# Test ds-mrs_fmrs
test_dataset "ds-mrs_fmrs" \
    "${TEST_DATA}/custom/ds-mrs_fmrs" \
    "${PLUGIN_DIR}/validation/configs/config_mrs.yaml" \
    "custom"

# Test ds-mtsat
test_dataset "ds-mtsat" \
    "${TEST_DATA}/custom/ds-mtsat" \
    "${PLUGIN_DIR}/validation/configs/config_mtsat.yaml" \
    "custom"

echo ""
echo "=== BIDS Examples Datasets ==="
echo ""

# Test asl001
test_dataset "asl001" \
    "${TEST_DATA}/bids-examples/asl001" \
    "${PLUGIN_DIR}/validation/configs/config_asl.yaml" \
    "bids"

# Test asl002
test_dataset "asl002" \
    "${TEST_DATA}/bids-examples/asl002" \
    "${PLUGIN_DIR}/validation/configs/config_asl.yaml" \
    "bids"

# Test qmri_irt1
test_dataset "qmri_irt1" \
    "${TEST_DATA}/bids-examples/qmri_irt1" \
    "${PLUGIN_DIR}/validation/configs/config_irt1.yaml" \
    "bids"

# Test qmri_megre
test_dataset "qmri_megre" \
    "${TEST_DATA}/bids-examples/qmri_megre" \
    "${PLUGIN_DIR}/validation/configs/config_megre.yaml" \
    "bids"

# Test qmri_mese
test_dataset "qmri_mese" \
    "${TEST_DATA}/bids-examples/qmri_mese" \
    "${PLUGIN_DIR}/validation/configs/config_mese.yaml" \
    "bids"

# Test qmri_mp2rage
test_dataset "qmri_mp2rage" \
    "${TEST_DATA}/bids-examples/qmri_mp2rage" \
    "${PLUGIN_DIR}/validation/configs/config_mp2rage.yaml" \
    "bids"

# Test qmri_mpm
test_dataset "qmri_mpm" \
    "${TEST_DATA}/bids-examples/qmri_mpm" \
    "${PLUGIN_DIR}/validation/configs/config_mpm.yaml" \
    "bids"

# Test qmri_mtsat - uses MTS named set
test_dataset "qmri_mtsat" \
    "${TEST_DATA}/bids-examples/qmri_mtsat" \
    "${PLUGIN_DIR}/validation/configs/config_mtsat.yaml" \
    "bids"

# Test qmri_sa2rage
test_dataset "qmri_sa2rage" \
    "${TEST_DATA}/bids-examples/qmri_sa2rage" \
    "${PLUGIN_DIR}/validation/configs/config_sa2rage.yaml" \
    "bids"

# Test qmri_tb1tfl
test_dataset "qmri_tb1tfl" \
    "${TEST_DATA}/bids-examples/qmri_tb1tfl" \
    "${PLUGIN_DIR}/validation/configs/config_tb1tfl.yaml" \
    "bids"

# Test qmri_vfa
test_dataset "qmri_vfa" \
    "${TEST_DATA}/bids-examples/qmri_vfa" \
    "${PLUGIN_DIR}/validation/configs/config_vfa.yaml" \
    "bids"

# Test eeg_cbm
test_dataset "eeg_cbm" \
    "${TEST_DATA}/bids-examples/eeg_cbm" \
    "${PLUGIN_DIR}/validation/configs/config_eeg.yaml" \
    "bids"

# Print summary
if [ "$LIST_ONLY" = true ]; then
    echo ""
    echo "BIDS Examples Datasets:"
    # The datasets will be listed above, now just print footer
    echo ""
    echo "Use: $0 [OPTIONS] [DATASETS...] to run tests"
    echo "Run: $0 -h for full help"
else
    echo ""
    echo "========================================="
    echo "Test Summary"
    echo "========================================="
    echo -e "Passed:  ${GREEN}${PASSED}${NC}"
    echo -e "Failed:  ${RED}${FAILED}${NC}"
    echo -e "Skipped: ${YELLOW}${SKIPPED}${NC}"
    echo "========================================="

    if [ ${FAILED} -gt 0 ]; then
        exit 1
    else
        exit 0
    fi
fi
