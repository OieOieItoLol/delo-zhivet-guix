#!/usr/bin/env python3

import argparse
import os
import sys
import subprocess
import re

def compute_guix_hash(filepath):
    try:
        # Run guix hash --format=base32
        result = subprocess.run(
            ['guix', 'hash', '--format=base32', filepath],
            capture_output=True,
            text=True,
            check=True
        )
        return result.stdout.strip()
    except FileNotFoundError:
        print("Error: guix command not found. Install Guix or run this script in a Guix environment.", file=sys.stderr)
        sys.exit(1)
    except subprocess.CalledProcessError as e:
        print(f"Error: guix hash failed for {filepath}:\n{e.stderr}", file=sys.stderr)
        sys.exit(1)

def main():
    parser = argparse.ArgumentParser(description="Fill Guix base32 hashes in packages.scm based on local artifacts.")
    parser.add_argument('--packages', default='output/delo-zhivet/packages.scm', help='Path to packages.scm')
    parser.add_argument('--dry-run', action='store_true', help='Do not modify the file, just print what would be done.')
    parser.add_argument('artifacts', nargs='*', help='Pairs of package-name artifact-path. e.g. delo-zhivet-backend-bin ./backend.jar')

    args = parser.parse_args()

    if len(args.artifacts) % 2 != 0:
        print("Error: Arguments must be in pairs of 'package-name' 'artifact-path'.", file=sys.stderr)
        sys.exit(1)

    packages_file = args.packages
    if not os.path.exists(packages_file):
        print(f"Error: {packages_file} does not exist.", file=sys.stderr)
        sys.exit(1)
    if not os.path.isfile(packages_file):
        print(f"Error: {packages_file} is not a file.", file=sys.stderr)
        sys.exit(1)

    updates = {}
    for i in range(0, len(args.artifacts), 2):
        pkg_name = args.artifacts[i]
        art_path = args.artifacts[i+1]

        if not os.path.exists(art_path):
            print(f"Error: Artifact path does not exist: {art_path}", file=sys.stderr)
            sys.exit(1)
        if not os.path.isfile(art_path):
            print(f"Error: Artifact path is not a file: {art_path}", file=sys.stderr)
            sys.exit(1)

        updates[pkg_name] = art_path

    # Read the file
    with open(packages_file, 'r', encoding='utf-8') as f:
        content = f.read()

    # Pre-check all markers exist before calculating hashes (fail fast)
    for pkg_name in updates.keys():
        marker = f";; HASH-MARKER: {pkg_name}"
        if marker not in content:
            print(f"Error: Hash marker for '{pkg_name}' not found in {packages_file}", file=sys.stderr)
            sys.exit(1)

    # Calculate hashes
    new_hashes = {}
    for pkg_name, art_path in updates.items():
        if not args.dry_run:
            print(f"Calculating hash for {pkg_name} using {art_path}...")
        new_hashes[pkg_name] = compute_guix_hash(art_path)

    # Apply updates
    modified_content = content
    for pkg_name, new_hash in new_hashes.items():
        marker = f";; HASH-MARKER: {pkg_name}"
        # We need to replace the (sha256 (base32 "...")) line that follows the marker
        # Using regex to find the block after the marker
        pattern = re.escape(marker) + r'\s*\(\s*sha256\s*\(\s*base32\s*"([^"]+)"\s*\)\s*\)'

        def replacer(match):
            return f'{marker}\n       (sha256 (base32 "{new_hash}"))'

        new_modified_content = re.sub(pattern, replacer, modified_content, count=1)
        if new_modified_content == modified_content:
            print(f"Error: Could not find (sha256 (base32 ...)) block after marker for {pkg_name}.", file=sys.stderr)
            sys.exit(1)
        modified_content = new_modified_content

        if args.dry_run:
            print(f"Would update {pkg_name} to hash: {new_hash}")
        else:
            print(f"Updated {pkg_name} hash.")

    if not args.dry_run:
        with open(packages_file, 'w', encoding='utf-8') as f:
            f.write(modified_content)
        print(f"Successfully updated {packages_file}")

if __name__ == '__main__':
    main()
