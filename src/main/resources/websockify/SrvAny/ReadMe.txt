
Content:
=========================
Beep.exe            A Beep program to show the Service function was triggered.
                    *Does not work under Win7/Vista service environment.
Beepy.cmd           Beep test script file.
GPL.txt
InstBeepY.cmd       Sample file for install BeepY.cmd as a Service.
license.txt
RemoveBeepY.cmd     Sample file for remove service 'BeepY'
ReadMe.txt          This file
Sc.exe              MicroSoft Tool. A command line program used for
                    communicating with the NT Service Controller and services.
SrvAny.exe          Service shell with scheduler program  for XP or older than
                    XP O.S. version.

\Win7\SrvAny.exe    Service shell with scheduler program for
                    new O.S. likes Win7 / Win8 / 2008 Server ...
\Win7\Beepy.cmd     Test script file for Win7.
\Win7\InstBeepY.cmd Sample file for install BeepY.cmd as a Service for Win7.
\Win7\RemoveBeepY.cmd Sample file for remove service 'BeepY' for Win7.

.\src\*.*           Program sources



Sample for function test:
=========================
1:edit InstBeepY.cmd (need to change all the program/script full path name of
                      beepy.cmd to actual location.)

  ..\SrvAny -install BeepY -10sd30s D:\Service\SrvAny\Beepy.cmd
                                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^
2:run InstBeepY.cmd to install 'BeepY' service.
  System will delay 30 sec then run BeepY.cmd every 10 sec.
  
3:run RemoveBeepY.cmd to remove service 'BeepY'
