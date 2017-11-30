# GitHub API Client

A toy project for

- having yet another excuse to finally learn myself some Clojure for the greater good,
- getting a feel for how a GraphQL API in general and GitHub's flavor in particular, well, feels
  in practice,
- getting to know some Clojure libraries I always wanted to get my hands dirty on,
- finding out what [RocksDB](http://rocksdb.org/) might be good for. 

You will find this stuff only useful if you are interested in finding out how **not** to implement a proper
Clojure application.

## Installation

Eventually, after ```lein uberjar``` had it's say, this application will be packaged as a Docker container.
```make``` is your friend here:

```
[13:20|obergner@Olafs-iMac|github-api-client [master L|✚ 4…2] ]
$ make image
lein uberjar
Compiling github-api-client.app
13:20:55.729 [main                ] INFO            org.eclipse.jetty.util.log - Logging initialized @7205ms
Compiling github-api-client.conf
Compiling github-api-client.core
Compiling github-api-client.event-log
Compiling github-api-client.github-api
Compiling github-api-client.management-api
Compiling github-api-client.storage
Compiling github-api-client.task
Created /Users/obergner/work/clj/github-api-client/target/uberjar/github-api-client-1.0.0-SNAPSHOT.jar
Created /Users/obergner/work/clj/github-api-client/target/uberjar/github-api-client-1.0.0-SNAPSHOT-standalone.jar
docker build \
         --build-arg version=1.0.0-SNAPSHOT \
         --build-arg port=3000 \
         --build-arg managementApiPort=3100 \
         -t "github-api-client":1.0.0-SNAPSHOT .
Sending build context to Docker daemon  45.99MB
Step 1/20 : FROM openjdk:8u151-jre-slim
 ---> 837969d6f968
Step 2/20 : MAINTAINER Olaf Bergner <olaf.bergner@gmx.de>
 ---> Using cache
 ---> cf4cdb4df479
 (much more of the same ...)
Step 20/20 : CMD java -jar /app/app.jar
 ---> Running in b2b9ccc0118c
 ---> 5403064aabcb
Removing intermediate container b2b9ccc0118c
Successfully built 5403064aabcb
Successfully tagged github-api-client:1.0.0-SNAPSHOT
[13:21|obergner@Olafs-iMac|github-api-client [master L|✚ 4…2] ]
```

## Usage

Start this application as a Docker container:

```
[13:24|obergner@Olafs-iMac|github-api-client [master L|✚ 5…1] ]
$ make run
docker run -i -t --rm \
         --env-file=./config.env \
         --env=GH_API_TOKEN=YOUR_GITHUB_API_TOKEN \
         --publish=3000:3000 \
         --publish=3100:3100 \
         --name=""github-api-client"" \
         "github-api-client":1.0.0-SNAPSHOT
12:25:05.461 [main                ] INFO            org.eclipse.jetty.util.log - Logging initialized @2172ms
12:25:05.956 [main                ] INFO                         mount-up.core - >> starting.. #'github-api-client.conf/conf
12:25:05.958 [main                ] INFO                         mount-up.core - >> starting.. #'github-api-client.conf/params
12:25:05.959 [main                ] INFO                         mount-up.core - >> starting.. #'github-api-client.storage/db
12:25:05.963 [main                ] INFO                         mount-up.core - >> starting.. #'github-api-client.task/schedules
12:25:05.963 [main                ] INFO                         mount-up.core - >> starting.. #'github-api-client.management-api/management-api
12:25:05.972 [async-dispatch-1    ] INFO             github-api-client.storage - Starting RocksDB persistence service
12:25:05.973 [main                ] INFO      github-api-client.management-api - Starting management API on port [3100], using config [{:gh-api-url "https://api.github.com/graphql", :gh-api-token "54219fe2f3209a8f461de044b1e493f93c9a2b23", :rocksdb-path "/app/db", :management-api-port 3100}] ...
12:25:06.035 [main                ] INFO       org.eclipse.jetty.server.Server - jetty-9.2.z-SNAPSHOT
12:25:06.355 [main                ] INFO      o.e.jetty.server.ServerConnector - Started ServerConnector@58d4f783{HTTP/1.1}{0.0.0.0:3100}
12:25:06.355 [main                ] INFO       org.eclipse.jetty.server.Server - Started @3067ms
12:25:06.356 [main                ] INFO                         mount-up.core - >> starting.. #'github-api-client.app/app
12:25:06.361 [main                ] INFO                 github-api-client.app - ============================================================================
12:25:06.361 [main                ] INFO                 github-api-client.app -  Starting: github-api-client v. 1.0.0-SNAPSHOT
12:25:06.362 [main                ] INFO                 github-api-client.app -            A demo client for GitHub's GraphQL API
12:25:06.362 [main                ] INFO                 github-api-client.app - 
12:25:06.362 [main                ] INFO                 github-api-client.app -  Git-Commit: 34d8769001b548dda4c7885a0adee4e1de28eed5
12:25:06.362 [main                ] INFO                 github-api-client.app -  Git-Branch: master
12:25:06.362 [main                ] INFO                 github-api-client.app -  Git-Dirty : true
12:25:06.362 [main                ] INFO                 github-api-client.app - ============================================================================
12:25:06.362 [main                ] INFO                 github-api-client.app - 
12:25:06.363 [main                ] INFO                 github-api-client.app -                         :java-class-path: /app/app.jar
12:25:06.363 [main                ] INFO                 github-api-client.app -                                    :path: /usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
12:25:06.364 [main                ] INFO                 github-api-client.app -                 :sun-management-compiler: HotSpot 64-Bit Tiered Compilers
12:25:06.364 [main                ] INFO                 github-api-client.app -                         :java-vendor-url: http://java.oracle.com/
12:25:06.364 [main                ] INFO                 github-api-client.app -                                    :home: /root
12:25:06.364 [main                ] INFO                 github-api-client.app -            :ca-certificates-java-version: 20170531+nmu1
12:25:06.364 [main                ] INFO                 github-api-client.app -                           :user-language: en
12:25:06.365 [main                ] INFO                 github-api-client.app -                           :java-ext-dirs: /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext:/usr/java/packages/lib/ext
12:25:06.365 [main                ] INFO                 github-api-client.app -              :java-vm-specification-name: Java Virtual Machine Specification
12:25:06.365 [main                ] INFO                 github-api-client.app -                        :sun-java-command: /app/app.jar
12:25:06.365 [main                ] INFO                 github-api-client.app -                       :java-runtime-name: OpenJDK Runtime Environment
12:25:06.365 [main                ] INFO                 github-api-client.app -                     :sun-boot-class-path: /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/resources.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/sunrsasign.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jsse.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jce.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/charsets.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jfr.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/classes
12:25:06.366 [main                ] INFO                 github-api-client.app -                          :file-separator: /
12:25:06.366 [main                ] INFO                 github-api-client.app -                               :user-name: root
12:25:06.366 [main                ] INFO                 github-api-client.app -                             :gh-prs-last: 8
12:25:06.366 [main                ] INFO                 github-api-client.app -                               :user-home: /root
12:25:06.366 [main                ] INFO                 github-api-client.app -                          :sun-cpu-endian: little
12:25:06.367 [main                ] INFO                 github-api-client.app -               :java-specification-vendor: Oracle Corporation
12:25:06.367 [main                ] INFO                 github-api-client.app -                      :java-endorsed-dirs: /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/endorsed
12:25:06.367 [main                ] INFO                 github-api-client.app -                     :management-api-port: 3100
12:25:06.367 [main                ] INFO                 github-api-client.app -                             :java-vendor: Oracle Corporation
12:25:06.367 [main                ] INFO                 github-api-client.app -                                :hostname: e8052944e755
12:25:06.368 [main                ] INFO                 github-api-client.app -                            :rocksdb-path: /app/db
12:25:06.368 [main                ] INFO                 github-api-client.app -                                    :port: 3000
12:25:06.368 [main                ] INFO                 github-api-client.app -                      :sun-os-patch-level: unknown
12:25:06.368 [main                ] INFO                 github-api-client.app -                                 :gh-repo: tensorflow
12:25:06.368 [main                ] INFO                 github-api-client.app -                 :sun-io-unicode-encoding: UnicodeLittle
12:25:06.368 [main                ] INFO                 github-api-client.app -                          :java-io-tmpdir: /tmp
12:25:06.369 [main                ] INFO                 github-api-client.app -                   :sun-boot-library-path: /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/amd64
12:25:06.369 [main                ] INFO                 github-api-client.app -                 :java-specification-name: Java Platform API Specification
12:25:06.369 [main                ] INFO                 github-api-client.app -                    :java-runtime-version: 1.8.0_151-8u151-b12-1~deb9u1-b12
12:25:06.369 [main                ] INFO                 github-api-client.app -                    :java-awt-graphicsenv: sun.awt.X11GraphicsEnvironment
12:25:06.370 [main                ] INFO                 github-api-client.app -                                    :lang: C.UTF-8
12:25:06.370 [main                ] INFO                 github-api-client.app -                                    :term: xterm
12:25:06.370 [main                ] INFO                 github-api-client.app -                      :java-class-version: 52.0
12:25:06.370 [main                ] INFO                 github-api-client.app -                       :java-library-path: /usr/java/packages/lib/amd64:/usr/lib/x86_64-linux-gnu/jni:/lib/x86_64-linux-gnu:/usr/lib/x86_64-linux-gnu:/usr/lib/jni:/lib:/usr/lib
12:25:06.370 [main                ] INFO                 github-api-client.app -                              :os-version: 4.9.49-moby
12:25:06.370 [main                ] INFO                 github-api-client.app -                       :sun-java-launcher: SUN_STANDARD
12:25:06.370 [main                ] INFO                 github-api-client.app -                            :java-version: 1.8.0_151
12:25:06.371 [main                ] INFO                 github-api-client.app -                         :log-interval-ms: 30000
12:25:06.371 [main                ] INFO                 github-api-client.app -                              :gh-api-url: https://api.github.com/graphql
12:25:06.371 [main                ] INFO                 github-api-client.app -                             :awt-toolkit: sun.awt.X11.XToolkit
12:25:06.371 [main                ] INFO                 github-api-client.app -                                  :gh-org: tensorflow
12:25:06.371 [main                ] INFO                 github-api-client.app -                       :file-encoding-pkg: sun.io
12:25:06.372 [main                ] INFO                 github-api-client.app -                         :java-vm-version: 25.151-b12
12:25:06.372 [main                ] INFO                 github-api-client.app -                          :line-separator: 

12:25:06.372 [main                ] INFO                 github-api-client.app -                     :java-debian-version: 8u151-b12-1~deb9u1
12:25:06.372 [main                ] INFO                 github-api-client.app -                                :no-proxy: *.local, 169.254/16
12:25:06.372 [main                ] INFO                 github-api-client.app -                            :java-vm-info: mixed mode
12:25:06.372 [main                ] INFO                 github-api-client.app -                                :user-dir: /app
12:25:06.373 [main                ] INFO                 github-api-client.app -                          :java-vm-vendor: Oracle Corporation
12:25:06.373 [main                ] INFO                 github-api-client.app -              :java-specification-version: 1.8
12:25:06.373 [main                ] INFO                 github-api-client.app -                          :path-separator: :
12:25:06.373 [main                ] INFO                 github-api-client.app -                     :java-awt-printerjob: sun.print.PSPrinterJob
12:25:06.373 [main                ] INFO                 github-api-client.app -                     :sun-arch-data-model: 64
12:25:06.373 [main                ] INFO                 github-api-client.app -                            :gh-api-token: **********************************************
12:25:06.374 [main                ] INFO                 github-api-client.app -                         :sun-cpu-isalist: 
12:25:06.374 [main                ] INFO                 github-api-client.app -                                 :os-name: Linux
12:25:06.374 [main                ] INFO                 github-api-client.app -                           :user-timezone: Etc/UTC
12:25:06.374 [main                ] INFO                 github-api-client.app -                            :java-vm-name: OpenJDK 64-Bit Server VM
12:25:06.374 [main                ] INFO                 github-api-client.app -           :java-vm-specification-version: 1.8
12:25:06.374 [main                ] INFO                 github-api-client.app -                           :file-encoding: UTF-8
12:25:06.375 [main                ] INFO                 github-api-client.app -                                 :os-arch: amd64
12:25:06.375 [main                ] INFO                 github-api-client.app -                     :java-vendor-url-bug: http://bugreport.sun.com/bugreport/
12:25:06.375 [main                ] INFO                 github-api-client.app -                        :sun-jnu-encoding: UTF-8
12:25:06.376 [main                ] INFO                 github-api-client.app -                               :java-home: /usr/lib/jvm/java-8-openjdk-amd64/jre
12:25:06.376 [main                ] INFO                 github-api-client.app -            :java-vm-specification-vendor: Oracle Corporation
12:25:06.376 [main                ] INFO                 github-api-client.app - 
12:25:06.376 [main                ] INFO                 github-api-client.app - ============================================================================
12:25:06.376 [main                ] INFO                github-api-client.task - START: log last [8] pull requests in [tensorflow/tensorflow] every [30000] ms
12:25:06.425 [async-dispatch-1    ] INFO             github-api-client.storage - Opened RocksDB instance [org.rocksdb.RocksDB@47292af2] located at [/app/db] using options [org.rocksdb.Options@544fb40c]
12:25:36.384 [async-dispatch-3    ] INFO           github-api-client.event-log - Storing last [8] pull requests in [organization: tensorflow|repo: tensorflow] in event log ...
12:25:36.385 [async-dispatch-3    ] INFO          github-api-client.github-api - Fetching last [8] pull requests from [tensorflow/tensorflow] ...
12:25:37.410 [async-dispatch-3    ] INFO          github-api-client.github-api - [DONE] [tensorflow/tensorflow last 8] -> [8 pull requests]
12:25:37.418 [async-dispatch-3    ] INFO           github-api-client.event-log - [DONE] Stored [8] pull requests under key [pr:tensorflow:tensorflow:1512044737410]
...
```

## Options

GitHub API Client aspires to be a well-behaved 12-factor app and is therefore configured via environment variables.

### GitHub Access Token

This application requires a [GitHub OAuth token](https://developer.github.com/v4/guides/forming-calls/#authenticating-with-graphql) in ```profiles.clj``` - which is ignored by Git. A sample [profiles.sample.clj](./profiles.sample.clj) is included. Insert your token and copy it to ```profiles.clj```:

```
{:dev-private {:env {:gh-api-token "YOUR_GITHUB_API_TOKEN"}}}
```

## Customized REPL

This project uses the rather excellent [mount](https://github.com/tolitius/mount) library for clean and powerful state
management. It is therefore possible to start and stop this application - while disposing cleanly of any resources if need be - in the REPL. Commands for this have been added in namespace [user](./profiles/dev/src/user.clj), along with some other convenient helper functions:

```
[12:59|obergner@Olafs-iMac|github-api-client [master L|✚ 2] ]
$ lein repl
12:59:45.335 [main                ] INFO            org.eclipse.jetty.util.log - Logging initialized @5492ms

-----------------------------------------------------------------------------------------
Welcome to GitHub API Client's REPL. Here's an overview of some custom commands you
might find useful:

 * (start):
     start GitHub API Client, including all subsystems
 * (stop):
     stop GitHub API Client, taking care to stop all subsystems in reverse startup order
 * (restart):
     stop, then start again
 * (schedule interval-ms org repo last):
     schedule importing last `last` pull requests from GitHub repository `org`/`repo`
     every `interval-ms` milliseconds
 * (check-health):
     call GitHub API Client's /health endpoint
 * (put-schedule interval-ms org repo last):
     call GitHub API Client's /schedules endpoint, equivalent to (schedule ....) above

Enjoy
-----------------------------------------------------------------------------------------

nREPL server started on port 49817 on host 127.0.0.1 - nrepl://127.0.0.1:49817
REPL-y 0.3.7, nREPL 0.2.12
Clojure 1.8.0
Java HotSpot(TM) 64-Bit Server VM 1.8.0_112-b16
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

user=> (start)
13:00:27.277 [nREPL-worker-1      ] INFO                         mount-up.core - >> starting.. #'github-api-client.conf/conf
13:00:27.279 [nREPL-worker-1      ] INFO                         mount-up.core - >> starting.. #'github-api-client.conf/params
13:00:27.279 [nREPL-worker-1      ] INFO                         mount-up.core - >> starting.. #'github-api-client.storage/db
13:00:27.281 [nREPL-worker-1      ] INFO                         mount-up.core - >> starting.. #'github-api-client.task/schedules
...
```

## License

Copyright © 2017 Olaf Bergner <olaf.bergner AT gmx.de>

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
