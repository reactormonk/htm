# Hema Tournament Manager

## Components

1. HTM-Admin
2. HTM-Viewer
3. HTM-Lib

## Development environment

1. [Eclipse IDE](http://eclipse.org/downloads)
2. [Scala IDE for Scala 2.10](http://scala-ide.org/download/current.html) (with Play plugin)
3. [SBT](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html)

## Getting started

* Clone this repository
* Run `sbt container:start`

## OSX

* To get sbt admin to work under OSX you will need to run the following code before starting the admin thread.

	`export SBT_OPTS=-XX:MaxPermSize=256m`
	
## Installing under CentOS 6

Prerequisites (run as root or sudo):

[SBT](http://scalasbt.artifactoryonline.com/scalasbt/sbt-native-packages/org/scala-sbt/sbt/0.13.0/sbt.rpm)

`yum localinstall sbt.rpm`


[JRE](http://www.java.com/getjava/) File: jre-7u45-linux-x64.rpm

`yum localinstall jre-7u45-linux-x64.rpm`

[Scala](http://www.scala-lang.org/files/archive/scala-2.10.3.tgz)

`tar xvf scala-2.10.3.tgz`

`mv scala-2.10.3 /usr/lib`

`ln -s /usr/lib/scala-2.10.3 /usr/lib/scala`

`echo 'export PATH=$PATH:/usr/lib/scala/bin' > /etc/profile/scala.sh`
