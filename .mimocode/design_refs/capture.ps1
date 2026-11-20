# Capture 17 layar desain dari emulator → .mimocode/design_refs/app/<iter>/
param([string]$Iter = "iter1")
$ErrorActionPreference = "Stop"
$root = "D:\Semester 5\Mobile Application\JagaMataApp"
$adb = "C:\Users\Zaky\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$out = Join-Path $root ".mimocode\design_refs\app\$Iter"
New-Item -ItemType Directory -Force -Path $out | Out-Null

& (Join-Path $root "gradlew.bat") installDebug --console=plain | Select-Object -Last 2

# Warm-up sekali (cold start terlama)
& $adb shell am force-stop com.jagamata.app
& $adb shell am start -n com.jagamata.app/.MainActivity --es screen beranda | Out-Null
Start-Sleep -Seconds 5

$slugs = @(
  "onboarding1", "onboarding2", "onboarding3", "beranda", "pantau_live",
  "peringatan", "istirahat", "statistik_harian", "hadiah", "profil_anak",
  "dashboard_ortu", "pengaturan", "izin", "kalibrasi", "kunci",
  "privasi", "overlay_ditolak"
)

foreach ($slug in $slugs) {
  # force-stop tiap layar: am start tanpa ini meneruskan intent ke instance lama
  # (onNewIntent) sehingga extra "screen" tidak dibaca ulang.
  & $adb shell am force-stop com.jagamata.app
  Start-Sleep -Milliseconds 300
  & $adb shell am start -n com.jagamata.app/.MainActivity --es screen $slug | Out-Null
  Start-Sleep -Seconds 5
  # OnboardingFlow: page 2/3 lewat tap CTA "Lanjut" — koordinat PIKSEL (1080x2400),
  # center tombol bottom-anchored: x=540, y=2214
  if ($slug -eq "onboarding2") { & $adb shell input tap 540 2214; Start-Sleep -Milliseconds 1200 }
  if ($slug -eq "onboarding3") { & $adb shell input tap 540 2214; Start-Sleep -Milliseconds 1200; & $adb shell input tap 540 2214; Start-Sleep -Milliseconds 1200 }
  $file = Join-Path $out "$slug.png"
  cmd /c "`"$adb`" exec-out screencap -p > `"$file`""
  $size = (Get-Item $file).Length
  Write-Host "$slug -> $size bytes"
}
Write-Host "DONE -> $out"
