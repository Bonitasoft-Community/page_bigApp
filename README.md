# Bonita Information Gathering Application (BigApp)


## What BigApp does?

This page's main goal is to retrieve information needed (environment information, log files, configuration files) to troubleshoot issues on the product.

<img src="ScreenshotBigApp.png"/>

## What each section of BigApp do?
* Environment Details:
This section contains general information about the server where your bonita application is installed on.. More information is included in the final Zip file, but only the most relevant ones are displayed on the page.
* Log Files:
This section contains log files that the user could select so they will be added in the final Zip file.
* Setup Configuration:
When the checkbox is enabled a "setup.[sh|bat] pull" is performed so the configuration files are added into the final Zip that users can download. When the checkbox is not enabled, only log files and environmental details are included in the final Zip file.

* Download Files:
As its name indicates, this button is responsible for generating and downloading a zip file containing all the files needed to troubleshoot issues on the product.

## How to install BigApp on your server?

1) Log in as Administrator

2) Go to "Resources" then Click on "Add" on the left of the page

3) Import "custompage_bigapp.zip" then click on "Next" and then "Confirm"

4) Go to "Applications", click on "New" and create a new application

5) On the newely created application click in the three dots (actions column)

6) Go to "Pages", click on "Add" and choose "custompage_bigapp - BigApp" and choose a name for the URL (Example : http://localhost:8080/bonita/apps/bigApp/bigAppPage/)

7) On "Pages" click on "Set as Home page" (above the house icon).

8) Finally, access the newly application using the URL that you previously set.
