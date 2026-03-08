# GitHub Yukleme Notu

## 1) Repo olustur
GitHub'da bos repo ac.

## 2) Yerelden yukle
```bash
git init
git add .
git commit -m "Initial GitHub-ready backup"
git branch -M main
git remote add origin <REPO_URL>
git push -u origin main
```

## 3) Notlar
- Bu backup, build/vendor/apk ve patch arsivi dislanmis temiz kopyadir.
- Web tarafta `.env` dosyasi bilincli olarak dahil edilmemistir.
