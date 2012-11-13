## NumberPicker for Android

This project is an Android library project for a NumberPicker widget. This library has been tested on Android 2.1 through Android 4.0. The repository contains three parts
described below. All source code is licensed under the Apache 2.0 License.

### Project overview

* lib - This is the library project that can be included in an Android project. This contains all the necessary resources and code for the functioning widget.
* sample - This is an Android sample app that demonstrates the use of the NumberPicker widget in a layout, as part of a dialog or as a preference item.
* tests - A set of unit tests for the library project. 

### Submitting patches

Patches for the project are welcome from anyone. The recommended practice for submitting a change is to fork the project, make your changes and submit a pull request to merge in
your changes. It is recommended that you write tests for the functionality or bug you are fixing with your patch. 

### Changelog

2012-03-06 Luca Santarelli @hrkdroid

* Bug fix: Store the value of the TextView on keyboard input.

2011-10-24 Paul Bourke <pauldbourke@gmail.com>

* Updated library project to support the new SDK r14 tools.

2011-08-27 Xenofon Papadopoulos <xpapad@gmail.com>

* Added _setWrap()_ method

* Initialization will respect user-provided attributes in the layout, see the example in res/layout/main.xml

