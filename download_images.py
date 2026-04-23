import re
import os
import hashlib
import urllib.request
import json

def main():
    html_files = ['1.html', '2.html', '3.html', '4.html', '5.html', '6.html']
    urls = set()
    for f in html_files:
        if os.path.exists(f):
            print(f"Scanning {f}...")
            with open(f, encoding='utf-8') as file:
                content = file.read()
                found = re.findall(r'https://lh3.googleusercontent.com/aida-public/[^\"\'<> ]+', content)
                urls.update(found)
    
    print(f"Found {len(urls)} unique URLs")
    target_dir = os.path.join('app', 'src', 'main', 'res', 'drawable')
    os.makedirs(target_dir, exist_ok=True)
    mapping = {}
    
    for url in sorted(list(urls)):
        # Generate a safe filename from the URL
        h = hashlib.md5(url.encode()).hexdigest()[:10]
        fname = f"img_net_{h}.png"
        target_path = os.path.join(target_dir, fname)
        
        if not os.path.exists(target_path):
            try:
                print(f"Downloading {url} to {fname}...")
                urllib.request.urlretrieve(url, target_path)
            except Exception as e:
                print(f"Failed to download {url}: {e}")
        
        mapping[url] = fname

    with open('image_mapping.json', 'w', encoding='utf-8') as f:
        json.dump(mapping, f, indent=4)
    print("Mapping saved to image_mapping.json")

if __name__ == '__main__':
    main()
