import sys

# Read the file
with open('frontend-web/src/app/features/admin/admin.component.html', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace enabled with isActive
content = content.replace('matColumnDef="enabled"', 'matColumnDef="isActive"')
content = content.replace('user.enabled', 'user.isActive')

# Write back
with open('frontend-web/src/app/features/admin/admin.component.html', 'w', encoding='utf-8') as f:
    f.write(content)

print("Successfully updated admin.component.html")
