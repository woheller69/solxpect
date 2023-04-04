<pre>Send a coffee to woheller69@t-online.de 
<a href= "https://www.paypal.com/signin"><img  align="left" src="https://www.paypalobjects.com/webstatic/de_DE/i/de-pp-logo-150px.png"></a></pre>

# pvCaster

pvCaster forecasts the output of your solar power plant

This app takes direct and diffuse radiation data from Open-Meteo.com, calculates the position
of the sun and projects the radiation on your solar panel.
It shows the estimated energy production for the next hours and up to 16 days.

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/01.png" width="150"/><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/02.png" width="150"/> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/03.png" width="150"/> 


## Parameters

#### Latitude [°] 
Latitude specifies the north–south position of your solar power plant. It ranges from –90° at the south pole to 90° at the north pole.

#### Longitude [°]
Longitude specifies the east–west position of your solar power plant. The prime meridian defines 0° longitude. Positive longitudes are east of the prime meridian, negative ones are west.

#### Azimuth [°]
Azimuth is the horizontal direction of your solar power plant. 0° equals North, 90° equals East, 180° equals South, 270° equals West.

#### Tilt [°]
Tilt is the vertical direction of your solar power plant. 0° means it points up towards the the sky, 90° means it has a vertical orientation and points towards the horizon.

#### Cells max power [W]
Maximum power your solar cells can deliver.

#### Cells efficiency  [%]
Portion of energy in the form of sunlight that can be converted into electricity by the solar cell.

#### Cell area [m\u00b2]
Size of the active area your solar panel.

#### Diffuse radiation efficiency  [%]
Efficiency of your solar power plant for diffuse radiation. When pointing up it should be around 100%, when pointing to the horizon it may be around 50%. 
Also depends on reflections etc.

#### Inverter power  [W]
Maximum power of your inverter. If it is lower than the maximum power of your panels the output power of your system will be limited by this parameter.

#### Inverter efficiency  [%] 
Efficiency of your inverter.

## License

This app is licensed under the GPLv3.

The app uses:
- Parts from Privacy Friendly Weather (https://github.com/SecUSo/privacy-friendly-weather) which is licensed under the GPLv3
- The weather data service is provided by [Open-Meteo](https://open-meteo.com/), under <a href='http://creativecommons.org/licenses/by/4.0/'>Attribution 4.0 International (CC BY 4.0)</a>
- Icons from [Google Material Design Icons](https://material.io/resources/icons/) licensed under <a href='http://www.apache.org/licenses/LICENSE-2.0'>Apache License Version 2.0</a>
- Material Components for Android (https://github.com/material-components/material-components-android) which is licensed under <a href='https://github.com/material-components/material-components-android/blob/master/LICENSE'>Apache License Version 2.0</a>
- Leaflet which is licensed under the very permissive <a href='https://github.com/Leaflet/Leaflet/blob/master/FAQ.md'>2-clause BSD License</a>
- WilliamChart (com.db.chart) (https://github.com/diogobernardino/williamchart) which is licensed under <a href='http://www.apache.org/licenses/LICENSE-2.0'>Apache License Version 2.0</a>
- Android Volley (com.android.volley) (https://github.com/google/volley) which is licensed under <a href='https://github.com/google/volley/blob/master/LICENSE'>Apache License Version 2.0</a>
- AndroidX libraries (https://github.com/androidx/androidx) which is licensed under <a href='https://github.com/androidx/androidx/blob/androidx-main/LICENSE.txt'>Apache License Version 2.0</a>
- AutoSuggestTextViewAPICall (https://github.com/Truiton/AutoSuggestTextViewAPICall) which is licensed under <a href='https://github.com/Truiton/AutoSuggestTextViewAPICall/blob/master/LICENSE'>Apache License Version 2.0</a>
- Map data from OpenStreetMap, licensed under the Open Data Commons Open Database License (ODbL) by the OpenStreetMap Foundation (OSMF) (https://www.openstreetmap.org/copyright)
- Solar positioning library (https://github.com/klausbrunner/solarpositioning) which is licensed under <a href='https://github.com/klausbrunner/solarpositioning/blob/master/LICENSE.txt'>MIT License</a>
## Contributing

If you find a bug, please open an issue in the Github repository, assuming one does not already exist.
  - Clearly describe the issue including steps to reproduce when it is a bug. In some cases screenshots can be supportive.
  - Make sure you mention the Android version and the device you have used when you encountered the issue.
  - Make your description as precise as possible.

If you know the solution to a bug please report it in the corresponding issue and if possible modify the code and create a pull request.

## Try my other apps

| **RadarWeather** | **Gas Prices** | **Smart Eggtimer** | 
|:---:|:---:|:---:|
| [<img src="https://github.com/woheller69/weather/blob/main/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.weather/)| [<img src="https://github.com/woheller69/spritpreise/blob/main/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.spritpreise/) | [<img src="https://github.com/woheller69/eggtimer/blob/main/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.eggtimer/) |
| **Level** | **hEARtest** | **GPS Cockpit** |
| [<img src="https://github.com/woheller69/Level/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.level/) | [<img src="https://github.com/woheller69/audiometry/blob/new/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.audiometry/) | [<img src="https://github.com/woheller69/gpscockpit/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.gpscockpit/) |
| **Audio Analyzer** | **LavSeeker** | **TimeLapseCam** |
| [<img src="https://github.com/woheller69/audio-analyzer-for-android/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.audio_analyzer_for_android/) |[<img src="https://github.com/woheller69/lavatories/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.lavatories/) | [<img src="https://github.com/woheller69/TimeLapseCamera/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.TimeLapseCam/) |
| **Arity** | **omWeather** | **pvCaster** |
| [<img src="https://github.com/woheller69/arity/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.arity/) | [<img src="https://github.com/woheller69/omweather/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.omweather/) | [<img src="https://github.com/woheller69/pvcaster/blob/main/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.pvcaster/) |
