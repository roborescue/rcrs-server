DAY=final2
for dir in ~/kernel-logs/$DAY/*; do
    name=$(basename "$dir")

    # Skip if name contains -precompute or output file already exists
    if [[ "$name" == *-precompute* || -e logs/$DAY/kernel/$name.xz ]]; then
        continue
    fi

    # Copy if source file exists
    if [ -f "$dir/rescue.log.xz" ]; then
       echo cp "$dir/rescue.log.xz" "~/logs/$DAY/kernel/$name.xz"
    fi
done