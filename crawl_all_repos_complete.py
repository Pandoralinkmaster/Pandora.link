#!/usr/bin/env python3
"""
COMPLETE SOURCE CODE EXTRACTION FROM ALL 109 GITHUB REPOSITORIES
Crawlt ALLE Links und speichert den KOMPLETTEN Source Code in TXT
"""

import json
import subprocess
import os
from pathlib import Path
from datetime import datetime
import sys

# Lade alle Links
with open('/home/ubuntu/extracted_zip/jona-ai-rebuilder/data-links.json', 'r') as f:
    data = json.load(f)
    links = data

# GitHub Links filtern
github_links = [link for link in links if 'github.com' in link.get('url', '')]
print(f"Total GitHub links to process: {len(github_links)}")

# Output-Datei
output_file = Path('/home/ubuntu/exports/ALL_SOURCE_CODE_FROM_ALL_REPOS.txt')
temp_repos = Path('/home/ubuntu/temp_repos_all')
temp_repos.mkdir(exist_ok=True)

# Statistiken
stats = {
    'total_repos_attempted': 0,
    'total_repos_success': 0,
    'total_files': 0,
    'total_lines': 0,
    'total_bytes': 0,
    'by_language': {},
    'start_time': datetime.now().isoformat(),
    'processed_repos': [],
    'failed_repos': []
}

print(f"\nStarting COMPLETE code extraction from ALL {len(github_links)} repositories...")
print(f"Output file: {output_file}\n")

# Schreibe Header
with open(output_file, 'w', encoding='utf-8', errors='ignore') as out:
    out.write("=" * 150 + "\n")
    out.write("COMPLETE SOURCE CODE FROM ALL 109 GITHUB REPOSITORIES\n")
    out.write("=" * 150 + "\n")
    out.write(f"Generated: {datetime.now().isoformat()}\n")
    out.write(f"Total Repositories: {len(github_links)}\n")
    out.write("=" * 150 + "\n\n")

# Code-Erweiterungen
code_extensions = {
    '.java', '.js', '.ts', '.tsx', '.jsx', '.py', '.go', '.c', '.cpp', '.h', '.hpp',
    '.cs', '.kt', '.scala', '.rb', '.php', '.swift', '.m', '.mm', '.rs', '.sh',
    '.bash', '.pl', '.r', '.lua', '.vim', '.sql', '.xml', '.json', '.yaml', '.yml',
    '.html', '.css', '.scss', '.less', '.vue', '.gradle', '.maven', '.pom', '.properties',
    '.conf', '.cfg', '.ini', '.toml', '.lock', '.gradle', '.makefile'
}

# Verarbeite ALLE Repositories
with open(output_file, 'a', encoding='utf-8', errors='ignore') as out:
    for repo_idx, link in enumerate(github_links, 1):
        repo_url = link.get('url', '')
        
        try:
            repo_name = repo_url.split('/')[-1].replace('.git', '')
            owner = repo_url.split('/')[-2]
        except:
            continue
        
        print(f"[{repo_idx:3d}/{len(github_links)}] {owner}/{repo_name}...", end=' ', flush=True)
        
        repo_path = temp_repos / f"{owner}_{repo_name}"
        stats['total_repos_attempted'] += 1
        
        try:
            # Clone Repository
            if not repo_path.exists():
                result = subprocess.run(
                    ['git', 'clone', '--depth', '1', '--quiet', repo_url, str(repo_path)],
                    capture_output=True,
                    timeout=120,
                    text=True
                )
                
                if result.returncode != 0:
                    print("CLONE FAILED")
                    stats['failed_repos'].append(f"{owner}/{repo_name}")
                    continue
            
            # Zähle und extrahiere Code-Dateien
            file_count = 0
            repo_lines = 0
            repo_bytes = 0
            
            for root, dirs, files in os.walk(repo_path):
                # Ignoriere große/unnötige Verzeichnisse
                dirs[:] = [d for d in dirs if d not in [
                    '.git', 'node_modules', '.venv', 'venv', 'dist', 'build',
                    '__pycache__', '.gradle', '.maven', 'target', 'bin', 'obj'
                ]]
                
                for file in files:
                    file_path = Path(root) / file
                    
                    if file_path.suffix.lower() in code_extensions:
                        try:
                            with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                                content = f.read()
                                lines = content.split('\n')
                                file_size = len(content)
                                
                                # Schreibe Datei-Header
                                out.write("\n" + "=" * 150 + "\n")
                                out.write(f"REPOSITORY: {owner}/{repo_name}\n")
                                out.write(f"FILE: {file_path.relative_to(repo_path)}\n")
                                out.write(f"EXTENSION: {file_path.suffix}\n")
                                out.write(f"LINES: {len(lines)}\n")
                                out.write(f"BYTES: {file_size}\n")
                                out.write("=" * 150 + "\n\n")
                                
                                # Schreibe Code mit Zeilennummern
                                for line_num, line in enumerate(lines, 1):
                                    out.write(f"{line_num:8d} | {line}\n")
                                
                                out.write("\n")
                                
                                # Update Statistiken
                                file_count += 1
                                repo_lines += len(lines)
                                repo_bytes += file_size
                                stats['total_files'] += 1
                                stats['total_lines'] += len(lines)
                                stats['total_bytes'] += file_size
                                
                                lang = file_path.suffix.lower()
                                if lang not in stats['by_language']:
                                    stats['by_language'][lang] = {'files': 0, 'lines': 0}
                                stats['by_language'][lang]['files'] += 1
                                stats['by_language'][lang]['lines'] += len(lines)
                        
                        except Exception as e:
                            pass
            
            stats['total_repos_success'] += 1
            stats['processed_repos'].append({
                'name': f"{owner}/{repo_name}",
                'files': file_count,
                'lines': repo_lines,
                'bytes': repo_bytes
            })
            
            print(f"✓ {file_count} files, {repo_lines:,} lines")
        
        except Exception as e:
            print(f"ERROR: {str(e)[:30]}")
            stats['failed_repos'].append(f"{owner}/{repo_name}")

# Speichere Statistiken
stats['end_time'] = datetime.now().isoformat()
with open('/home/ubuntu/exports/COMPLETE_CODE_EXTRACTION_STATS.json', 'w') as f:
    json.dump(stats, f, indent=2)

# Zeige Ergebnisse
print(f"\n{'='*80}")
print(f"✓ COMPLETE CODE EXTRACTION FINISHED!")
print(f"{'='*80}")
print(f"Repositories attempted: {stats['total_repos_attempted']}")
print(f"Repositories successful: {stats['total_repos_success']}")
print(f"Total code files: {stats['total_files']:,}")
print(f"Total lines: {stats['total_lines']:,}")
print(f"Total bytes: {stats['total_bytes'] / (1024*1024):.2f} MB")
print(f"Output file: {output_file}")
print(f"Output file size: {output_file.stat().st_size / (1024*1024):.2f} MB")

# Top Languages
print(f"\nTop Languages by Lines:")
sorted_langs = sorted(stats['by_language'].items(), key=lambda x: x[1]['lines'], reverse=True)
for lang, data in sorted_langs[:20]:
    print(f"  {lang}: {data['files']:5d} files, {data['lines']:12,d} lines")

if stats['failed_repos']:
    print(f"\nFailed repositories ({len(stats['failed_repos'])}):")
    for repo in stats['failed_repos'][:10]:
        print(f"  - {repo}")

print(f"\n{'='*80}")
print(f"Complete source code from ALL {stats['total_repos_success']} repositories saved!")
print(f"File: {output_file}")
print(f"{'='*80}")
