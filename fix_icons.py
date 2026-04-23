"""Fix all invalid icon references and problematic Chinese quote strings in Kotlin files."""
import os, re

SAFE_REPLACEMENTS = {
    "Icons.Outlined.Description": "Icons.Outlined.Article",
    "Icons.Outlined.Schedule": "Icons.Outlined.DateRange",
    "Icons.Outlined.LocalOffer": "Icons.Outlined.Tag",
    "Icons.Outlined.ModeComment": "Icons.Outlined.Comment",
    "Icons.Outlined.Book": "Icons.Outlined.Article",
    "Icons.Outlined.Opacity": "Icons.Outlined.Opacity",  # May still fail, use Tonality
}

FINAL_SAFE = {
    "Icons.Outlined.Article": "Icons.Outlined.Feed",
    "Icons.Outlined.Tag": "Icons.Outlined.Info",
    "Icons.Outlined.Comment": "Icons.Outlined.Forum",
    "Icons.Outlined.Opacity": "Icons.Outlined.Spa",
    "Icons.Outlined.Description": "Icons.Outlined.Feed",
    "Icons.Outlined.Schedule": "Icons.Outlined.DateRange",
    "Icons.Outlined.LocalOffer": "Icons.Outlined.Info",
    "Icons.Outlined.ModeComment": "Icons.Outlined.Forum",
    "Icons.Outlined.Book": "Icons.Outlined.Feed",
    "Icons.Outlined.RemoveRedEye": "Icons.Outlined.Visibility",
    "Icons.Outlined.Book,": "Icons.Outlined.Feed,",
    "Icons.Outlined.Opacity,": "Icons.Outlined.Spa,",
}

VERIFIED_SAFE_ICONS = {
    "Icons.Outlined.Description": "Icons.Outlined.Feed",
    "Icons.Outlined.Schedule": "Icons.Outlined.DateRange",
    "Icons.Outlined.LocalOffer": "Icons.Outlined.Info",
    "Icons.Outlined.ModeComment": "Icons.Outlined.Forum",
    "Icons.Outlined.Opacity": "Icons.Outlined.Spa",
    "Icons.Outlined.ChatBubble,": "Icons.Outlined.Forum,",
    "Icons.Outlined.Book,": "Icons.Outlined.Feed,",
}


def fix_file(path):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original = content
    
    for bad, good in VERIFIED_SAFE_ICONS.items():
        content = content.replace(bad, good)
    
    # Fix the Chinese opening/closing curly quotes in string literals
    # The issue: Text("大橘今天看起来...", ) where the first " is a curly quote starting in Kotlin
    # Replace curly double-quotes with corner brackets where they appear inside string literals
    content = content.replace('"\u5927\u6a58\u4eca\u5929\u770b\u8d77\u6765\u5fc3\u60c5\u4e0d\u9519\uff0c\u559d\u4e86\u4e0d\u5c11\u6c34\u3002"', '\\u300c\u5927\u6a58\u4eca\u5929\u770b\u8d77\u6765\u5fc3\u60c5\u4e0d\u9519\uff0c\u559d\u4e86\u4e0d\u5c11\u6c34\u3002\\u300d')
    
    if content != original:
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed: {path}")
    else:
        print(f"No changes: {path}")

screens_dir = os.path.join('app', 'src', 'main', 'java', 'com', 'example', 'myapplication', 'ui', 'screens')
for fname in os.listdir(screens_dir):
    if fname.endswith('.kt'):
        fix_file(os.path.join(screens_dir, fname))
