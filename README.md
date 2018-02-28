### Install Nmap Manually

**NOTE:** I am assuming you have SU access on your phone
1. Download Nmap data and binaries for your phone's architecture
    - https://github.com/kost/nmap-android/releases
2. On phone create "/sdcard/opt/nmap-7.31/bin" directory
```bash
$ adb shell
# su
# mkdir -p /sdcard/opt/nmap-7.31/bin
```
3. Extract Nmap binary to "/sdcard/opt/nmap-7.31/bin/" path on phone
```bash
$ unzip nmap-7.31-binaries-architecture.zip -d bin
$ adb push bin/* /sdcard/opt/nmap-7.31/bin/
```
4. Extract Nmap data to "/sdcard/opt/" path on phone
```bash
$ unzip nmap-7.31-data.zip -d data
$ adb push data/* /sdcard/opt
```
5. Create "/data/user/0/com.example.swu.ndroidmap/files/nmap" directory on phone
```bash
$ adb shell
$ su
# mkdir -p /data/user/0/com.example.swu.ndroidmap/files/nmap
```
6. Copy Nmap executable to "/data/user/0/com.example.swu.ndroidmap/files/nmap/" directory on phone
```bash
$ adb push bin/nmap /data/user/0/com.example.swu.ndroidmap/files/nmap/
```
7. Give execute permissions to Nmap executable
```bash
$ adb shell
$ su
# chmod +x /data/user/0/com.example.swu.ndroidmap/files/nmap/nmap
```
For more info: https://secwiki.org/w/Nmap/Android

### Usage
- To start scanning:
    1.  Enter the desired Nmap arguments on the input line
        - The input line functions similarly to running Nmap from the command line. It should
        accept any valid Nmap parameter
    2. Tap RUN
    3. The output from Nmap will appear in the output text box after the scan has finished
- To Stop the scan:
    1. Tap the STOP button
- The FOUND tab attempts to parse the output from the scan creating a list of IP addresses/ports that
were found. To reset the list tap CLEAR.

### Errors
Try:
- Grant WRITE_EXTERNAL_STORAGE permissions to the app.
- Check that the correct binary is installed.
- If a scan takes too long try stopping and scanning a smaller address space
- If output is cut off try hiding/showing the appbar or toolbar which can be done through the
navigation drawer

### Other Info.
- Rooting your phone may provide more information from the Nmap scans.
- Only tested on Samsung S7 and Nexus5
    - Nmap download/install failures may be caused by my code not correctly identifying 
    other phones' architecture. In this case try installing Nmap manually.
