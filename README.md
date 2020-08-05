# Bonita Information Gathering Application (BigApp)


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

1) Download the latest version of bigApp from the Release tab;

2) Log in as Administrator

3) Go to "Resources" then Click on "Add" on the left of the page ;

4) Import "custompage_bigapp.zip" then click on "Next" and then "Confirm" ;

5) Dowload the applicationDescriptorFile of BigApp by right-click [here](https://raw.githubusercontent.com/Bonitasoft-Community/page_bigApp/master/applicationDescriptorFile.xml) and select the "Save content as..." ;

6) Go to "Applications", click on "Import" and select the .xml file saved on the last step ;

7) Finally, access the newly application using the URL "../bonita/apps/bigApp/BonitaGatheringApp/"
