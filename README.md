# Yulia's Workout

An Android app with a home-screen widget. The 2×1 widget shows Iron Kettle, a cast-iron kettlebell that gets heavier and angrier the longer today's workout waits, next to the current streak. The app has one button that opens the workout site and one button that marks today's workout as done.

- `design/`: the design previews. `kettle.html` is the chosen design, and the widget pictures are rendered from it.
- `app/`: the Android app (Kotlin, no extra libraries).
- `.github/workflows/build.yml`: builds the APK on every push. On `main`, it also publishes it as a GitHub release.

## Mood schedule

| Time | Picture |
| --- | --- |
| Until 12:00 | Morning, relaxed and smiling |
| 12:00 to 17:00 | Afternoon, hands on hips, impatient |
| 17:00 to 21:00 | Evening, angry with a donut |
| 21:00 to midnight | Late night, crying, cracked mirror |
| Marked done | Slim and flexing |

Missing a day resets the streak to 0.
