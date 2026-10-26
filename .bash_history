cat << 'EOF' > /sdcard/APK_Studio/apk_cyber_studio.py
import os
import sys

try:
    import arabic_reshaper
    from bidi.algorithm import get_display
    def ar(text):
        return get_display(arabic_reshaper.reshape(text))
except ImportError:
    def ar(text):
        return text

WORKDIR = "/sdcard/APK_Studio"
APKTOOL = os.path.join(WORKDIR, "apktool.jar")
UBER_SIGNER = os.path.join(WORKDIR, "uber-signer.jar")

# الألوان
C = '\033[96m'
G = '\033[92m'
R = '\033[91m'
Y = '\033[93m'
W = '\033[0m'
B = '\033[1m'

def clear():
    os.system('clear')

def banner():
    clear()
    print(f"{C}{B}==============================================")
    print(f"   ⚡ APK CYBER STUDIO v3.1 PRO (Termux)      ")
    print(f"   💻 {ar('المطور: ياسر بنعيسى')} | YACIR BEN AISSA    ")
    print(f"==============================================")
    print(f"{G}● {ar('البيئة جاهزة - التوقيع المتقدم (V1/V2/V3) مفعل')}{W}\n")

def run_cmd(cmd):
    print(f"\n{Y}[EXEC]{W} > {cmd}\n")
    res = os.system(cmd)
    if res == 0:
        print(f"\n{G}[✓] {ar('تمت العملية بنجاح!')}{W}")
    else:
        print(f"\n{R}[✗] {ar('حدث خطأ أثناء التنفيذ.')}{W}")
    input(f"\n{C}{ar('اضغط Enter للمتابعة...')}{W}")

def main():
    while True:
        banner()
        print(f"{B}[1]{W} 🔓 {ar('تفكيك كامل')} (Full Decompile)")
        print(f"{B}[2]{W} 📜 {ar('تفكيك السورس فقط')} (-r)")
        print(f"{B}[3]{W} 🎨 {ar('تفكيك الموارد فقط')} (-s)")
        print(f"{B}[4]{W} 🔒 {ar('إعادة بناء التطبيق')} (Rebuild APK)")
        print(f"{B}[5]{W} 🔑 {ar('توقيع متقدم تلقائي')} (Auto Sign V1/V2/V3)")
        print(f"{B}[6]{W} 📦 {ar('تثبيت الإطار')} (Install Framework)")
        print(f"{B}[7]{W} 📄 {ar('استخراج المانفيست')} (Dump Manifest)")
        print(f"{B}[8]{W} 📲 {ar('تثبيت APK على الهاتف')}")
        print(f"{B}[0]{W} 🚪 {ar('خروج')}")
        
        choice = input(f"\n{Y}{ar('اختر رقماً')} [0-8]: {W}").strip()
        
        if choice == '1':
            apk = input(f"{C}{ar('أدخل مسار/اسم ملف APK:')} {W}").strip()
            if apk:
                out = os.path.splitext(apk)[0]
                run_cmd(f'java -jar "{APKTOOL}" d "{apk}" -o "{out}" -f')
        elif choice == '2':
            apk = input(f"{C}{ar('أدخل مسار/اسم ملف APK:')} {W}").strip()
            if apk:
                out = os.path.splitext(apk)[0] + "_src"
                run_cmd(f'java -jar "{APKTOOL}" d -r "{apk}" -o "{out}" -f')
        elif choice == '3':
            apk = input(f"{C}{ar('أدخل مسار/اسم ملف APK:')} {W}").strip()
            if apk:
                out = os.path.splitext(apk)[0] + "_res"
                run_cmd(f'java -jar "{APKTOOL}" d -s "{apk}" -o "{out}" -f')
        elif choice == '4':
            folder = input(f"{C}{ar('أدخل مسار المجلد المفكك:')} {W}").strip()
            if folder:
                out = f"{folder}_modded.apk"
                run_cmd(f'java -jar "{APKTOOL}" b "{folder}" -o "{out}"')
        elif choice == '5':
            apk = input(f"{C}{ar('أدخل مسار ملف APK للتوقيع:')} {W}").strip()
            if apk:
                run_cmd(f'java -jar "{UBER_SIGNER}" --apks "{apk}"')
        elif choice == '6':
            apk = input(f"{C}{ar('أدخل مسار ملف APK الإطار:')} {W}").strip()
            if apk:
                run_cmd(f'java -jar "{APKTOOL}" if "{apk}"')
        elif choice == '7':
            apk = input(f"{C}{ar('أدخل مسار ملف APK:')} {W}").strip()
            if apk:
                out = os.path.splitext(apk)[0] + "_manifest"
                run_cmd(f'java -jar "{APKTOOL}" d -s -r "{apk}" -o "{out}" -f')
        elif choice == '8':
            apk = input(f"{C}{ar('أدخل مسار ملف APK للتثبيت:')} {W}").strip()
            if apk:
                run_cmd(f'termux-open "{apk}"')
        elif choice == '0':
            print(f"\n{G}{ar('بالتوفيق! تم إغلاق APK Cyber Studio.')}{W}")
            break

if __name__ == "__main__":
    main()
EOF

cd /sdcard/APK_Studio && python apk_cyber_studio.py
exit
~/APK-Cyber-Studio-Android
gh auth login
git init
git config user.name "YACIR BEN AISSA"
git config user.email "your_email@example.com"
git add .
git commit -m "Initial commit - APK Cyber Studio Native"
gh repo create APK-Cyber-Studio-Android --public --source=. --remote=origin --push
git rm -r --cached .config
cat << 'EOF' > .gitignore
.config/
.gradle/
build/
app/build/
*.apk
EOF

git add .gitignore
git commit --amend -m "Initial commit - APK Cyber Studio Native"
git push -u origin master --force
exit
