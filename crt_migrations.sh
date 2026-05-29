#!/bin/bash

MIGRATION_DIR="src/main/resources/db/migration"
mkdir -p "$MIGRATION_DIR"

DATE=$(date +%Y%m%d)

# Get next sequence number - ShellCheck compliant
NEXT_SEQ=1
if [ -d "$MIGRATION_DIR" ]; then
    # Use array to store matching files
    files=("$MIGRATION_DIR"/V"${DATE}"_*.sql)
    
    # Check if any files exist
    if [ ${#files[@]} -gt 0 ] && [ -e "${files[0]}" ]; then
        # Get the last file (highest sequence)
        last_file="${files[-1]}"
        
        # Extract sequence number using parameter expansion (no grep)
        basename=$(basename "$last_file")
        # Remove prefix V${DATE}_ and suffix __*.sql to get sequence
        seq_part="${basename#V"${DATE}"_}"
        seq="${seq_part%%__*}"
        
        # Remove leading zeros and increment
        seq_num=$((10#$seq))
        NEXT_SEQ=$((seq_num + 1))
    fi
fi

SEQ=$(printf "%03d" $NEXT_SEQ)
DESC=$(echo "$1" | tr ' ' '_' | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9_]//g')
FILENAME="V${DATE}_${SEQ}__${DESC}.sql"

cat > "$MIGRATION_DIR/$FILENAME" << EOF
-- Migration: $1
-- Date: $(date '+%Y-%m-%d %H:%M:%S')

-- UP
-- TODO: Write your migration

-- DOWN (optional)
-- TODO: Write rollback if needed

EOF

echo "✅ Created: $MIGRATION_DIR/$FILENAME"
