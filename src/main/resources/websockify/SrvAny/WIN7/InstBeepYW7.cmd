rem
rem     Run every 10 seconds with delay 30 seconds first.
rem
rem    This batch must be executed as administrator user
rem
SrvAny -install BeepY -10sd30s D:\Service\Exe\Win7\Beepy.cmd
sc config BeepY start= auto

net start BeepY
pause
