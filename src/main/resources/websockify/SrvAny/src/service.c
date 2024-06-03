//
// Modify by Pedro P. Wong.
//
//  MODULE:   service.c
//
//  PURPOSE:  Implements functions required by all services
//
//  FUNCTIONS:
//    main(int argc, char **argv);
//    service_ctrl(DWORD dwCtrlCode);
//    service_main(DWORD dwArgc, LPTSTR *lpszArgv);
//    CmdInstallService();
//    CmdRemoveService();
//    CmdRunService(int argc, char **argv);
//    ControlHandler ( DWORD dwCtrlType );
//    GetLastErrorText( LPTSTR lpszBuf, DWORD dwSize );
//
//  COMMENTS:
//
//  Modify by: Pedro P. Wong
//
// #define  Win7 1

#include <windows.h>
#include <stdio.h>
#include <conio.h>
#include <stdlib.h>
#include <process.h>
#include <tchar.h>

#include "service.h"

// internal variables
SERVICE_STATUS          ssStatus;       // current status of the service
SERVICE_STATUS_HANDLE   sshStatusHandle;
DWORD                   dwErr = 0;
BOOL                    bDebug = FALSE;
BOOL                    instfg = FALSE;
BOOL					removefg = FALSE;
TCHAR                   szErr[256],
                        LoopCount[256],
                        SrvParm[256],
                        WorkPath[256],
						SrvName[256],
						SrvAny[]="SrvAny";

INT     i;		      // global working
BOOL    LoopCountFG = FALSE; // Service Name Flag
BOOL    SrvExeFG    = FALSE; // Run Program Name & parameters Flag

// internal function prototypes
VOID WINAPI service_ctrl(DWORD dwCtrlCode);
VOID WINAPI service_main(DWORD dwArgc, LPTSTR *lpszArgv);
VOID CmdInstallService();
VOID CmdRemoveService();
VOID CmdRunService(int argc, char **argv);
BOOL WINAPI ControlHandler ( DWORD dwCtrlType );
LPTSTR GetLastErrorText( LPTSTR lpszBuf, DWORD dwSize );
SERVICE_TABLE_ENTRY dispatchTable[] =
    {
        { (LPSTR) SrvAny, (LPSERVICE_MAIN_FUNCTION)service_main },
//        { TEXT(SZSERVICENAME), (LPSERVICE_MAIN_FUNCTION)service_main },
        { NULL, NULL }
    };

//
//  FUNCTION: main
//
//  PURPOSE: entrypoint for service
//
//  PARAMETERS:
//    argc - number of command line arguments
//    argv - array of command line arguments
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//    main() either performs the command line task, or
//    call StartServiceCtrlDispatcher to register the
//    main service thread.  When the this call returns,
//    the service has stopped, so exit.
//
int main(int argc, char *argv[], char *envp[])
{
	  // check if Win32s, if so, display notice and terminate
      if( GetVersion() & 0x80000000 )
      {
        MessageBox( NULL,
           "This application cannot run on Win3.1, Win95, Win98.\n"
           "This application will now terminate.\n",
           "SrvAny",
           MB_OK | MB_ICONSTOP | MB_SETFOREGROUND );
        exit(0);
      }

  lstrcpy(LoopCount, ""); // Set default name
  lstrcpy(SrvParm, ""); // Set default name
  lstrcpy(WorkPath, ""); // Set default name
  lstrcpy(SrvName, ""); // Set default name

  if (argc < 1) goto messageout;

  for (i=1; i< argc; i++)
  {
//
//      Flag process
//
	if  ( _stricmp(argv[i], "-remove") == 0)
		{
           removefg = TRUE;
		   continue;
		} 
	if  (stricmp(argv[i], "-install") == 0)
		{
           instfg = TRUE;
		   continue;
		} 
	if  (stricmp(argv[i], "-debug") == 0)
		{
           bDebug = TRUE;
		   continue;
		} 
	if  (stricmp(argv[i], "?") == 0) 
		   goto messageout;
//
//      Parameter process
//
	if (LoopCountFG)          // First parameter is service name
		{
            if (SrvExeFG)   // append all parameters also
			{
                lstrcat(SrvParm, " ");
                lstrcat(SrvParm,argv[i]);
			} else {        // copy first Execution parameter         
                lstrcpy(SrvParm,argv[i]);
			    lstrcpy(WorkPath, argv[i]);
		        SrvExeFG = TRUE;
			}
	} else { // Set Service name had been copy-in
			lstrcpy(LoopCount, argv[i]);
			// For install/remove mode this parameter is Service name.
			lstrcpy(SrvName, argv[i]);
			LoopCountFG = TRUE;
	}
  }
	if  (stricmp(LoopCount, "") == 0) goto messageout;
	if  (stricmp(SrvParm, "") == 0 && !removefg) goto messageout;

    printf( "LoopCount >%s<, ServiceParm >%s< \n", LoopCount, SrvParm);
//
//      Execute request function
//
    if (instfg) {
          CmdInstallService();    // install
	      exit(0);
    } else if (removefg) {
          CmdRemoveService();     // remove
	      exit(0);
	} 
    CmdRunService(argc, argv);    // run in service mode
	exit(0);
//
//   Message out
//
  messageout:

	    printf( "\n");
		printf( "%s is a service shell program,  can be used to\n", SZAPPNAME);
		printf( "   1:install a SrvAny service.\n", SZAPPNAME);
		printf( "   2:remove a service\n   3:run as an SrvAny service.\n   The format was defined as following:\n\n");
        printf( "-- to install a service\n   >%s -install ServiceName [-debug] LoopInterval <App.& param>\n", SZAPPNAME );
		printf( "      Will install application as:\n");
		printf( "           %s.exe [-debug] LoopInterval <App. name&path with param>\n", SZAPPNAME );
		printf( "           means that System will run %s as a service program\n", SZAPPNAME);
		printf( "           and %s provide a service shell and run the application\n", SZAPPNAME);
		printf( "           program every LoopInterval seconds/minutes under %s with/without\n", SZAPPNAME);
		printf( "           debug inf. dump.\n", SZAPPNAME);
		printf( "           LoopInterval format: xxxx[s|S][d|Dyyyy[s|S]]\n");
		printf( "              xxxx = run every xxxx minutes\n");
		printf( "              xxxx{s|S} = run every xxxx seconds (Min. value is 2 sec.)\n");
		printf( "              if xxxx less than 0 means delay xxxx or yyyy first.\n");
		printf( "              yyyy = delay yyyy minutes first.\n");
		printf( "              yyyy{s|S} = delay yyyy seconds first.\n");
        printf( "      (note fully-qualified path name to .exe)\n");
        printf( "-- to remove a service\n   >%s -remove  ServiceName\n", SZAPPNAME );
        printf( "-- to run an app as service program W/O dump\n   >%s LoopInterval <App. with param>\n", SZAPPNAME );
        printf( "-- to run an app with inf. dump in event log\n   >%s -debug LoopInterval <App. with param>\n", SZAPPNAME );
		printf( "\n");
#ifdef Win7
		printf("   Win7 Version, ");
#else
		printf("   ");
#endif
		printf("Copyright (C) Pedro P. Wong,  All Rights Reserved. 12/25/'2013\n");
		printf("   EMail: pedrowong2001@yahoo.com.tw\n");
		printf("   Official Site: http://simpleauto.host-ed.me/home/\n");
		printf("                  http://simpleauto.byethost8.com/\n\n>");
		//i = getc(stdin);
		i = getch();
}
//
//  FUNCTION: service_main
//
//  PURPOSE: To perform actual initialization of the service
//
//  PARAMETERS:
//    dwArgc   - number of command line arguments
//    lpszArgv - array of command line arguments
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//    This routine performs the service initialization and then calls
//    the user defined ServiceStart() routine to perform majority
//    of the work.
//
void WINAPI service_main(DWORD dwArgc, LPTSTR *lpszArgv)
{
    //
    // register our service control handler:
    //
    sshStatusHandle = RegisterServiceCtrlHandler( (LPSTR)SrvAny, service_ctrl);
//    sshStatusHandle = RegisterServiceCtrlHandler( TEXT(SZSERVICENAME), service_ctrl);

    if (!sshStatusHandle) goto cleanup;

    // SERVICE_STATUS members that don't change in example
    //
    ssStatus.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
    ssStatus.dwServiceSpecificExitCode = 0;

    // report the status to the service control manager.
    //
    if (!ReportStatusToSCMgr(
        SERVICE_START_PENDING, // service state
        NO_ERROR,              // exit code
        3000))                 // wait hint
        goto cleanup;

	i = lstrlen(WorkPath);
	if (!i)  goto cleanup;
   	do {
        i--;
	} while (WorkPath[i] != '\\' &&
		     WorkPath[i] != '/'  && 
		     WorkPath[i] != ':'  &&
			 i>0);
    WorkPath[i] = '\0';

    if (bDebug) {
        wsprintf((LPTSTR)szErr, "\nservice_main -- WorkPath = >%s<.\n",
		               (LPSTR)WorkPath);
	    AddToMessageLog(szErr, FALSE);
	}

    ServiceStart( dwArgc, lpszArgv );

cleanup:

    // try to report the stopped status to the service control manager.
    //
    if (sshStatusHandle)
        (VOID)ReportStatusToSCMgr(
                            SERVICE_STOPPED,
                            dwErr,
                            0);
    return;
}
//
//  FUNCTION: service_ctrl
//
//  PURPOSE: This function is called by the SCM whenever
//           ControlService() is called on this service.
//
//  PARAMETERS:
//    dwCtrlCode - type of control requested
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//
VOID WINAPI service_ctrl(DWORD dwCtrlCode)
{
    // Handle the requested control code.
    //
    switch(dwCtrlCode)
    {
        // Stop the service.
        //
        // SERVICE_STOP_PENDING should be reported before
        // setting the Stop Event - hServerStopEvent - in
        // ServiceStop().  This avoids a race condition
        // which may result in a 1053 - The Service did not respond...
        // error.
        case SERVICE_CONTROL_STOP:
            ReportStatusToSCMgr(SERVICE_STOP_PENDING,
				                NO_ERROR,
								0);
            ServiceStop();
            return;

        // Update the service status.
        //
        case SERVICE_CONTROL_INTERROGATE:
            break;

        // invalid control code
        //
        default:
            break;
    }

    ReportStatusToSCMgr(ssStatus.dwCurrentState, NO_ERROR, 0);
}
//
//  FUNCTION: ReportStatusToSCMgr()
//
//  PURPOSE: Sets the current status of the service and
//           reports it to the Service Control Manager
//
//  PARAMETERS:
//    dwCurrentState - the state of the service
//    dwWin32ExitCode - error code to report
//    dwWaitHint - worst case estimate to next checkpoint
//
//  RETURN VALUE:
//    TRUE  - success
//    FALSE - failure
//
//  COMMENTS:
//
BOOL ReportStatusToSCMgr(DWORD dwCurrentState,
                         DWORD dwWin32ExitCode,
                         DWORD dwWaitHint)
{
    static DWORD dwCheckPoint = 1;
    BOOL fResult = TRUE;

//    if ( !bDebug ) // when debugging we don't report to the SCM
//    {
        if (dwCurrentState == SERVICE_START_PENDING)
            ssStatus.dwControlsAccepted = 0;
        else
            ssStatus.dwControlsAccepted = SERVICE_ACCEPT_STOP;

        ssStatus.dwCurrentState = dwCurrentState;
        ssStatus.dwWin32ExitCode = dwWin32ExitCode;
        ssStatus.dwWaitHint = dwWaitHint;

        if ( ( dwCurrentState == SERVICE_RUNNING ) ||
             ( dwCurrentState == SERVICE_STOPPED ) )
            ssStatus.dwCheckPoint = 0;
        else
            ssStatus.dwCheckPoint = dwCheckPoint++;


        // Report the status of the service to the service control manager.
        //
        if (!(fResult = SetServiceStatus( sshStatusHandle,
			                              &ssStatus))) {
            AddToMessageLog(TEXT("\nSetServiceStatus error\n"), TRUE);
        }
//    }
    return fResult;
}
//
//  FUNCTION: AddToMessageLog(LPTSTR lpszMsg, BOOL Eflag)
//
//  PURPOSE: Allows any thread to log an error message
//
//  PARAMETERS:
//    lpszMsg - text for message
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//
VOID AddToMessageLog(LPTSTR lpszMsg, BOOL Eflag)
{
    TCHAR   szMsg[256];
    HANDLE  hEventSource;
    LPTSTR  lpszStrings[2];
	WORD    Emsg, MsgNo;

	if (Eflag) {
        Emsg = EVENTLOG_ERROR_TYPE;
		MsgNo = 2;
        dwErr = GetLastError();

        // Use event logging to log the error.
        //
//        hEventSource = RegisterEventSource(NULL, (LPSTR)LoopCount);
////        hEventSource = RegisterEventSource(NULL, TEXT(SZSERVICENAME));
//        _stprintf(szMsg, TEXT("%s error: %d"), TEXT(SZSERVICENAME), dwErr);
        _stprintf(szMsg, TEXT("%s error: %d"),
			      (LPSTR)SrvAny, dwErr);
		lpszStrings[0] = szMsg;
        lpszStrings[1] = lpszMsg;
	} else {
        Emsg = EVENTLOG_INFORMATION_TYPE;
        lpszStrings[0] = lpszMsg;
		MsgNo = 1;
	}

	hEventSource = RegisterEventSource(NULL,
		                    (LPSTR)SrvAny);

        if (hEventSource != NULL) {
            ReportEvent(hEventSource, // handle of event source
//                EVENTLOG_ERROR_TYPE,  // event type
                Emsg,                 // event type
                0,                    // event category
                0,                    // event ID
                NULL,                 // current user's SID
                MsgNo,                // strings in lpszStrings
                0,                    // no bytes of raw data
                lpszStrings,          // array of error strings
                NULL);                // no raw data

            (VOID) DeregisterEventSource(hEventSource);
        }
}
///////////////////////////////////////////////////////////////////
//
//  The following code handles service installation and removal
//
//
//  FUNCTION: CmdInstallService()
//
//  PURPOSE: Installs the service
//
//  PARAMETERS:
//    none
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//
void CmdInstallService()
{
    SC_HANDLE   schService;
    SC_HANDLE   schSCManager;

    TCHAR szPath[512];

//    if ( GetModuleFileName( NULL, szPath, 512 ) == 0 )
    if ( GetModuleFileName( NULL, WorkPath, 256 ) == 0 )
    {
        _tprintf(TEXT("Unable to install %s - %s\n"), 
			    (LPSTR)SrvAny, GetLastErrorText(szErr, 256));
        return;
    }

	if (bDebug) {
        _stprintf(szPath, TEXT("%s -debug %s"),
			      (LPSTR)WorkPath, (LPSTR)SrvParm);
	} else {
        _stprintf(szPath, TEXT("%s %s"),
			      (LPSTR)WorkPath, (LPSTR)SrvParm);
	}

    schSCManager = OpenSCManager(
                        NULL,                   // machine (NULL == local)
                        NULL,                   // database (NULL == default)
                        SC_MANAGER_ALL_ACCESS   // access required
                        );
    if ( schSCManager )
    {
        schService = CreateService(
            schSCManager,               // SCManager database
            (LPSTR)SrvName,             // name of service
            (LPSTR)SrvName,             // name to display
//            TEXT(SZSERVICENAME),        // name of service
//            TEXT(SZSERVICEDISPLAYNAME), // name to display
            SERVICE_ALL_ACCESS,         // desired access
            SERVICE_WIN32_OWN_PROCESS,  // service type
            SERVICE_DEMAND_START,       // start type
            SERVICE_ERROR_NORMAL,       // error control type
            szPath,                     // service's binary
            NULL,                       // no load ordering group
            NULL,                       // no tag identifier
            TEXT(SZDEPENDENCIES),       // dependencies
            NULL,                       // LocalSystem account
            NULL);                      // no password

        if ( schService )
        {
            _tprintf(TEXT("%s installed.\n"), (LPSTR)SrvAny );
            CloseServiceHandle(schService);
        }
        else
        {
            _tprintf(TEXT("CreateService failed - %s\n"),
				     GetLastErrorText(szErr, 256));
        }

        CloseServiceHandle(schSCManager);
    }
    else
        _tprintf(TEXT("OpenSCManager failed - %s\n"),
		         GetLastErrorText(szErr,256));
}
//
//  FUNCTION: CmdRemoveService()
//
//  PURPOSE: Stops and removes the service
//
//  PARAMETERS:
//    none
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//
void CmdRemoveService()
{
    SC_HANDLE   schService;
    SC_HANDLE   schSCManager;

    schSCManager = OpenSCManager(
                        NULL,                   // machine (NULL == local)
                        NULL,                   // database (NULL == default)
                        SC_MANAGER_ALL_ACCESS   // access required
                        );
    if ( schSCManager )
    {
        schService = OpenService(schSCManager,
			         (LPSTR)SrvName, SERVICE_ALL_ACCESS);
//			         TEXT(SZSERVICENAME), SERVICE_ALL_ACCESS);

        if (schService)
        {
            // try to stop the service
            if ( ControlService( schService, SERVICE_CONTROL_STOP, &ssStatus ) )
            {
                _tprintf(TEXT("Stopping %s."), (LPSTR)SrvName);
                Sleep( 1000 );

                while( QueryServiceStatus( schService, &ssStatus ) )
                {
                    if ( ssStatus.dwCurrentState == SERVICE_STOP_PENDING )
                    {
                        _tprintf(TEXT("."));
                        Sleep( 1000 );
                    }
                    else
                        break;
                }

                if ( ssStatus.dwCurrentState == SERVICE_STOPPED )
                    _tprintf(TEXT("\n%s stopped.\n"), 
					        (LPSTR)SrvAny );
                else
                    _tprintf(TEXT("\n%s failed to stop.\n"),
					        (LPSTR)SrvAny );
            }
            // now remove the service
            if( DeleteService(schService) )
                _tprintf(TEXT("%s removed.\n"), (LPSTR)SrvName );
            else
                _tprintf(TEXT("DeleteService failed - %s\n"),
				         GetLastErrorText(szErr,256));

            CloseServiceHandle(schService);
        }
        else
            _tprintf(TEXT("OpenService failed - %s\n"),
			         GetLastErrorText(szErr,256));

        CloseServiceHandle(schSCManager);
    }
    else
        _tprintf(TEXT("OpenSCManager failed - %s\n"),
		         GetLastErrorText(szErr,256));
}
///////////////////////////////////////////////////////////////////
//
//  FUNCTION: CmdRunService(int argc, char ** argv)
//
//  PURPOSE: Runs the program as a service specified by
//           command line arguments
//
//  PARAMETERS:
//    argc - number of command line arguments
//    argv - array of command line arguments
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//
void CmdRunService(int argc, char ** argv)
{
    DWORD dwArgc;
    LPTSTR *lpszArgv;

#ifdef UNICODE
    lpszArgv = CommandLineToArgvW(GetCommandLineW(),
		       &(dwArgc) );
#else
    dwArgc   = (DWORD) argc;
    lpszArgv = argv;
#endif

    if (bDebug) {
	    wsprintf((LPTSTR)szErr, "\nCmdRunService -- Running SrvAny by %s.\n",
		                    (LPSTR)LoopCount);
	    AddToMessageLog(szErr, FALSE);
	}
	if (!StartServiceCtrlDispatcher(dispatchTable))
        AddToMessageLog(TEXT("\nStartServiceCtrlDispatcher failed.\n"), TRUE);

    SetConsoleCtrlHandler(ControlHandler, TRUE);

    ServiceStart(dwArgc, lpszArgv);
}
//
//  FUNCTION: ControlHandler ( DWORD dwCtrlType )
//
//  PURPOSE: Handled control events
//
//  PARAMETERS:
//    dwCtrlType - type of control event
//
//  RETURN VALUE:
//    True - handled
//    False - unhandled
//
//  COMMENTS:
//
BOOL WINAPI ControlHandler ( DWORD dwCtrlType )
{
    switch( dwCtrlType )
    {
        case CTRL_BREAK_EVENT:  // use Ctrl+C or Ctrl+Break to simulate
        case CTRL_C_EVENT:      // SERVICE_CONTROL_STOP in debug mode
              if (bDebug) {
	               wsprintf((LPTSTR)szErr,
					        "SERVICE_CONTROL_STOP >%s<.\n",
					        (LPSTR)LoopCount);
	               AddToMessageLog(szErr, FALSE);
			  }
            ServiceStop();
            return TRUE;
            break;
    }
    return FALSE;
}
//
//  FUNCTION: GetLastErrorText
//
//  PURPOSE: copies error message text to string
//
//  PARAMETERS:
//    lpszBuf - destination buffer
//    dwSize - size of buffer
//
//  RETURN VALUE:
//    destination buffer
//
//  COMMENTS:
//
LPTSTR GetLastErrorText( LPTSTR lpszBuf, DWORD dwSize )
{
    DWORD dwRet;
    LPTSTR lpszTemp = NULL;

    dwRet = FormatMessage( FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM |FORMAT_MESSAGE_ARGUMENT_ARRAY,
                           NULL,
                           GetLastError(),
                           LANG_NEUTRAL,
                           (LPTSTR)&lpszTemp,
                           0,
                           NULL );

    // supplied buffer is not long enough
    if ( !dwRet || ( (long)dwSize < (long)dwRet+14 ) )
        lpszBuf[0] = TEXT('\0');
    else
    {
        lpszTemp[lstrlen(lpszTemp)-2] = TEXT('\0');  //remove cr and newline character
        _stprintf( lpszBuf, TEXT("%s (0x%x)"),
			       lpszTemp, GetLastError());
    }

    if ( lpszTemp )
        LocalFree((HLOCAL) lpszTemp );

    return lpszBuf;
}
