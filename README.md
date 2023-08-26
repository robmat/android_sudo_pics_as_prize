# Android sudoku with pics as prize

Simple sudoku for android that rewards users with pictures after a win.

Work based on https://github.com/kaajjo/Libre-Sudoku

## How to run

1. Import into android studio
2. Add `prize-images` directory in `app/src/main/assets` and some prize images inside
3. Create `local.properties` file in root directory and add properties for AdMob:

```properties
manifest.ad.id=ca-app-pub-XXXXXXXXXXXXXXXXXXXXX
adhelper.ad.id=ca-app-pub-XXXXXXXXXXXXXXXXXXXXX
```