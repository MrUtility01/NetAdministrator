# انتشار پروژه در GitHub

این بسته برای قرارگرفتن در مخزن `MrUtility01/NetAdministrator` آماده شده است.

## روش پیشنهادی با Git

1. ZIP را در یک پوشه استخراج کن.
2. در GitHub مخزن مقصد را باز کن و اگر محتوای مهمی ندارد آن را clone کن.
3. فایل‌ها و پوشه‌های پروژهٔ اندروید را در ریشهٔ مخزن کپی کن.
4. از PowerShell در ریشه اجرا کن:

```powershell
git status
git add .
git commit -m "Add offline-first Android learning client"
git push origin main
```

## دریافت APK

پس از push، تب **Actions** را باز کن. Workflow با نام Android CI اجرا می‌شود و APK را به‌صورت Artifact پیوست می‌کند. برای انتشار Release، یک tag مثل `v1.0.0` بساز و push کن.
