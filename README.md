WLANAudit 3
===========
An application to scan for WLAN Access points and to audit their security and capabilities
The application shows relevant diagnostic information about an access point such as MAC Address, encryption, signal strength, etc.

On some access points the application tries to guess the access point default password via if a public known algorithm exists.

This app is **NOT MEANT** to be used as a hacking utility, so I'm not responsible of the usage you make of it.

Getting the sourcecode
======================
To get the source code clone the repository with the following commands
    
    $ git clone git://github.com/RobertoEstrada/WLANAudit-Android.git

Building the sourcecode
=======================
In order to build the sourcecode you need to have installed and configured Java, the Android SDK and Apache Ant. You need to set the environment variable ANDROID_HOME and have the Android tools and Ant in your path.

Then in a shell at the project root, use the following command

    $ ant

Working with Eclipse
====================
Open eclipse and select *File / Import / Existing projects into workspace* and then select the project root as the base directory. Select all the projects.

Work as usual. 

**DO NOT MODIFY THE LIBRARY PROJECTS FROM ECLIPSE AND DO NOT COMMIT THEM OR THE BUILD WILL BREAK**

Contributing to the project
=============================
Fork the project, make your changes and send a pull request for code review. Your pull requests must be self contained, otherwise they will be rejected until they meet that criteria.

In order to achieve that requirement do the following:

	1. Fork the project
	2. Create a topic branch, based on any of the existing brancges, for the feature that you want de develop
	3. Make the essential changes that your new feature requires
	4. Issue a pull request, be clear about the changes you've made.
	5. If your contribution passes code review, it will be integrated into the mainline.

More info on how to work with pull requests can be found in the article ['Using Pull Requests'] (https://help.github.com/articles/using-pull-requests)

Developed by
============
* Roberto Estrada

Contributors
============
* Jesús Manzano Camino

License
=======
>Copyright 2012 Roberto Estrada
>
>Licensed under the Apache License, Version 2.0 (the "License");
>you may not use this file except in compliance with the License.
>You may obtain a copy of the License at
>
>   http://www.apache.org/licenses/LICENSE-2.0
>
>Unless required by applicable law or agreed to in writing, software
>distributed under the License is distributed on an "AS IS" BASIS,
>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
>See the License for the specific language governing permissions and
>limitations under the License.



