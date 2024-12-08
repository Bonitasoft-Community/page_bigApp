# Bonita Information Gathering Application (BigApp)

![Bonita BPM](https://img.shields.io/badge/Bonita-BPM-blue?style=for-the-badge&logo=bonita)

## What BigApp does?

This page's main goal is to retrieve information (as environmental information, log files, configuration files) to troubleshoot potential issues on the product.

<img src="ScreenshotBigApp.png"/>

## What each section of BigApp do?
* Environmental information:
This section contains general information about the server where your bonita application is installed on. More information is included in the final Zip file, but only the most relevant ones are displayed on the page.
* Log Files:
This section contains log files that the user could select so they will be added in the final Zip file. PS: Current day's logs are auto-selected, but the user could unselect them if needed.
* Setup Configuration:
When the checkbox is enabled a "setup.[sh|bat] pull" is performed so the configuration files are added into the final Zip that users can download. When the checkbox is not enabled, only log files and environmental details are included in the final Zip file.

* Download Files:
As its name indicates, this button is responsible for generating and downloading a zip file containing all the files needed to troubleshoot issues on the product.
 
## How to install BigApp on your server?

1. Download the latest version of BigApp from the [Releases tab](https://github.com/Bonitasoft-Community/page_bigApp/releases).
2. Log in as Administrator in Bonita.
3. Go to **Resources**, click **Add**, and import `custompage_bigapp.zip`.
4. Download the application descriptor file [here](https://raw.githubusercontent.com/Bonitasoft-Community/page_bigApp/master/BigAppApplicationDescriptorFile.xml) and save it.
5. Go to **Applications**, click **Import**, and select the saved `.xml` file.
6. Access the app via: `../bonita/apps/bigApp/BonitaGatheringApp/`

---

## License

Distributed under the **GPL-3.0 License**. See [LICENSE](./LICENSE) for more details.

---

## Contribute

1. Fork the project.
2. Create your feature branch (`git checkout -b feature/awesome-feature`).
3. Commit your changes (`git commit -m 'Add an awesome feature'`).
4. Push to the branch (`git push origin feature/awesome-feature`).
5. Open a Pull Request.
