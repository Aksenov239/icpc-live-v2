icpc-live
==============

This version of overlays for competitive programming broadcasts was used from 2015 til 2021. Please, refer to https://github.com/icpc/live-v3 for the latest version.

==============
Requirements:
* Windows
* OBS Studio
* Java 1.8 (sorry, Java upgrade is in process)
* Maven

To Run:
* checkout the repository
* install OBS shared memory plugin dll https://drive.google.com/file/d/1MvCmhlSpftUFC3N2gj0Lv88-ZV2dtnhP/view?usp=sharing
* create config/events.properties and config/mainscreen.properties files according to your contest (refer to archive/ for examples)
* run webserver.bat
* if successful -- open http://localhost:8080 and use default admin and password to access web interface
* run mainscreen.bat
* if successful -- run OBS with source "Overlay Master"

Currently Supported CMS:
* PCMS2 http://pcms.itmo.ru
* ICPC World Finals CMS https://tools.icpc.global/cds/
* Codeforces https://codeforces.com
