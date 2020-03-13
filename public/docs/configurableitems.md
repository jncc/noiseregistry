# Configurable Items

There are a number of items that can be configured through text files in the system.

The system will need to be redeployed after changes.

## Messages

Although internationalisation was not a requirement for this project, the system has been designed with i118n internationalisation in mind in case there is a future need.  The messages file (`/conf/messages`) contains almost all strings output by the application, and these can all be configured by changing the text in this file. This includes:
- labels, field help, error messages, etc.
- the code for Google Analytics
- the static text for the initial welcome, feedback, help, cookies, contact and terms and conditions pages.  

Where multi-line text is required for an item in the messages file, each line prior to the last line must be terminated with a back slash (`\`).

## Emails

All of the Scala templates for emails are in the `/app/views.email` directory.  The names of these should be self-explanatory.  All emails use messages in the messages file, so almost all changes can be made there without touching the templates.

The weekly notifications are scheduled using a `CRON` syntax in the application.conf file (`email.weekly.cron`).

## PDF files

There are set of three PDF files that can be downloaded through the web interface (*TODO*: These should be translated to templates and pushed into HTML);
 - Help file - `/public/help/marine-noise-registry-help-and-guidance.pdf`
 - Privacy Notice - `/public/help/marine-noise-registry-privacy-notice.pdf`
 - Terms and conditions - `/public/help/marine-noise-registry-terms.pdf` 

To update a file, replace it with one which has the same file name.
