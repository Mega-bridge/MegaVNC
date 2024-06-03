rem
rem     Run every 10 seconds with delay 30 seconds first.
rem
rem     Change Beepy.cmd path before runs this install
rem
rem 1: Install service 'BeepY'
SrvAny -install BeepY -10sd30s S:\VC\SrvAny-N\SrvAny\Beepy.cmd
rem
rem 2: Configure BeepY service parameter for auto start up
rem
sc config BeepY start= auto
rem
rem 3: start up the service.
rem
net start BeepY
pause
