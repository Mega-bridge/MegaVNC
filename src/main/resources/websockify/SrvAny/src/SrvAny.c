//
// Modify Pedro P. Wong.
//
//  MODULE:   SrvAny.c
//
//  PURPOSE:  Implements the body of the service.
//            The default behavior is run the program
//            specified in parameters in service mode.
//
//  FUNCTIONS:
//            ServiceStart(DWORD dwArgc, LPTSTR *lpszArgv);
//            ServiceStop( );
//
//  COMMENTS: The functions implemented in arvany.c are
//            prototyped in service.h
//              

#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <process.h>
#include <tchar.h>

#include <direct.h>

#include <time.h>

#include "service.h"

extern TCHAR LoopCount[256];
extern TCHAR SrvParm[256];
extern TCHAR WorkPath[256];
extern TCHAR szErr[256];
extern BOOL bDebug;
char CurDir[256];
BOOL StopFg = FALSE;

time_t   Time1x,		/* Standard binary time for Trigger time */
	     Time2x;		/* Standard binary time for current time */
int Tinc,				/* increase value per loop interval */
    Tdel,               /* delay time before start schedule */
    Sflag;				/* second mode flag */

//  debug to file flag

//OFSTRUCT   rOF; 
//int        nResult;
//char       dump[256];

// this event is signalled when the
// service should end
//
HANDLE  hServerStopEvent = NULL;

     STARTUPINFO si;
     PROCESS_INFORMATION pi;
	 DWORD ExitCode;
//
//  FUNCTION: ServiceStart
//
//  PURPOSE: Actual code of the service
//           that does the work.
//
//  PARAMETERS:
//    dwArgc   - number of command line arguments
//    lpszArgv - array of command line arguments
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//            Implements the body of the service.
//            The default behavior is run the program
//            specified in parameters in service mode.
//
VOID ServiceStart (DWORD dwArgc, LPTSTR *lpszArgv)
{
	int i, j, K, n;

    HANDLE          hEvents[2] = {NULL, NULL};

//                         2/20/2003
//	  Stop the program in case of program 
//     was restart in case of service stop.
	if (StopFg) goto cleanup;

    ///////////////////////////////////////////////////
    //
    // Service initialization
    //

    // report the status to the service control manager.
    //
    if (bDebug)   // Copy Right, All Rights Reserved.
	    AddToMessageLog(TEXT("\nServiceStart -- Service Start    Copy Right Pedro P. Wong,  All Rights Reserved.\n"),
		               FALSE);

    if (!ReportStatusToSCMgr(
        SERVICE_START_PENDING, // service state
        NO_ERROR,              // exit code
        3000))                 // wait hint
        goto cleanup;

    // create the event object. The control handler function signals
    // this event when it receives the "stop" control code.
    //
    if (bDebug)
	    AddToMessageLog(TEXT("\nServiceStart -- Create Event.\n"), FALSE);

    hServerStopEvent = CreateEvent(
        NULL,    // no security attributes
        TRUE,    // manual reset event
        FALSE,   // not-signalled
        NULL);   // no name

    if ( hServerStopEvent == NULL)
        goto cleanup;

    hEvents[0] = hServerStopEvent;

    // report the status to the service control manager.
    //
    if (!ReportStatusToSCMgr(
        SERVICE_START_PENDING, // service state
        NO_ERROR,              // exit code
        3000))                 // wait hint
        goto cleanup;

    // report the status to the service control manager.
    //
    if (!ReportStatusToSCMgr(
        SERVICE_RUNNING,       // service state
        NO_ERROR,              // exit code
        0))                    // wait hint
        goto cleanup;

    //
    // End of initialization
    //
    ////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////
    //
    // Service is now running, perform work until shutdown
    //

    // change Dir. to Application Dir. 

    GetCurrentDirectory(256, CurDir);  // save Dir. for dump
	if (strlen(WorkPath)) {
		if (_chdir(WorkPath)) {
   			wsprintf((LPTSTR)szErr, "\nServiceStart -- Change Dir. Error.\n LoopCount = %s\n, SrvParm = %s\n, Start Dir. = %s\n, Working Dir. = %s\n",
			         LoopCount, SrvParm, CurDir, WorkPath);
			AddToMessageLog(szErr, TRUE);
			goto cleanup;
		}
	}
//
//    Dump Data
//
	if (bDebug) {

//		lstrcpy(dump, WorkPath);     // dump to file
//		lstrcat (dump, "\\dump.txt");
//		nResult = OpenFile((LPSTR)dump,
//		               &rOF, OF_CREATE);
//		if (nResult == -1) goto cleanup;

    	    wsprintf((LPTSTR)szErr, "\nServiceStart -- LoopCount = %s\n, SrvParm = %s\n, Start Dir. = %s\n, Working Dir. = %s\n",
			         LoopCount, SrvParm, CurDir, WorkPath);
 	        AddToMessageLog(szErr, FALSE);
//		    _lwrite(nResult, szErr, lstrlen(szErr));
	}

	if (bDebug) {
		wsprintf((LPTSTR)szErr, "\nServiceStart -- CreateProcess. %s\n", CurDir);
	    AddToMessageLog(szErr, FALSE);
//	    _lwrite(nResult, szErr, lstrlen(szErr));
	}

	/*	Skip old submit program function

	lstrcpy(CurDir, LoopCount);   // Create process name and parameters
	lstrcat(CurDir, " ");
	lstrcat(CurDir, SrvParm);
    */
    /* Launch the process and waits for it to complete */

	/*	Skip old submit program function

    si.cb = sizeof(STARTUPINFO);
    si.lpReserved = NULL;
    si.lpReserved2 = NULL;
    si.cbReserved2 = 0;
    si.lpTitle = (LPSTR) SrvParm; 
    si.lpDesktop = (LPTSTR)NULL;
    si.dwFlags = STARTF_FORCEONFEEDBACK;

    if (!CreateProcess(NULL,
        CurDir,
        NULL,
        NULL,
        FALSE,
        NORMAL_PRIORITY_CLASS,
        NULL,
        (LPTSTR)NULL,
        &si,
        &pi)) {
           AddToMessageLog(TEXT("\nServiceStart -- CreateProcess failed.\n"),
			               TRUE);
           goto cleanup;
        }
    if (bDebug)
	    AddToMessageLog(TEXT("\nServiceStart -- Waiting for complete.\n"),
		                FALSE);
//		_lwrite(nResult, TEXT("\nServiceStart -- Waiting for complete.\n"),
//			            39);
//		_lclose(nResult);
    */

    /* wait for the program exist. */
	/*	Skip old submit program function
    WaitForSingleObject(pi.hProcess, INFINITE);
	*/
	/* New Function -- schedule the program */
	if (!lstrlen(SrvParm)) {
		AddToMessageLog(TEXT("\nServiceStartError -- No Schedule program error.\n"),
		                FALSE);
		goto cleanup;
	}
	n = 0;
	sscanf((const char *)&LoopCount,"%i%n",&i,&n);
	Tinc = i;

	//Tinc = i = atoi(LoopCount);

	/*
	j = strlen(LoopCount);
	if (LoopCount[j-1]=='s' ||
		LoopCount[j-1]=='S') {
	*/
	if (LoopCount[n]=='s' ||
		LoopCount[n]=='S') {
		 Sflag = 1;
		 if (i == 1) Tinc = i = 2;	/* minimun is 2 sec. */
		 else if (i == -1) Tinc = i = -2;
		 n++;
	} else Sflag = 0;
	/* Fatch delay time */
	if (i<0) Tdel = -Tinc;
	else Tdel = 0;
	if (!Sflag) Tdel = Tdel * 60;

	if (LoopCount[n]) {
		if (LoopCount[n]=='d' ||
			LoopCount[n]=='D') {
			n++;
			j = 0;
			sscanf((const char *)&LoopCount+n,"%i%n",&n,&j);
			if (j && n) {
				j = strlen(LoopCount);
				if (LoopCount[j-1]=='s' ||
					LoopCount[j-1]=='S') {
					// delay by seconds
					Tdel = n;
				} else {
					// delay by minutes
					Tdel = n * 60;
				}
			}
		}
	}
	if (bDebug) {
		wsprintf((LPTSTR)szErr, "\nSrvAny -- Tinc = %i, Tdel = %i, Sflag = %i\n",
				 Tinc, Tdel, Sflag);
	    AddToMessageLog(szErr, FALSE);
	}

	if (i< 0) {			// Delay first (i<0)
		Tinc = i = -i;
		//goto Loop01;
		if (Tdel>0) Sleep(1000*Tdel);
	}
	/* Fatch current time */
	time( &Time1x);

	goto Submit01;		// Run program

Loop01:
	if (StopFg) goto cleanup;
	// ======== new ===========
	/* New Time compare function */
	if (Sflag) Time1x = Time1x+ Tinc;
	else Time1x = Time1x + Tinc*60;

Loop02:
	if (StopFg) goto cleanup;
	if (Sflag) Sleep(1000);		/* check per second for sec. mode */
	else       Sleep(20*1000);	/* check per 20 second for min. mode */
	time (&Time2x);
	if (Time2x<Time1x) goto Loop02;
	// ======== new ===========

	// j = strlen(LoopCount);
	// if (LoopCount[j-1]=='s' || LoopCount[j-1]=='S') {
		/*
		Loop by seconds
		*/
		// Sleep(i*1000);
	// } else {
		/*
		Loop by minutes
		*/
		// for (j=0;j<i*2;j++) {
				// if (StopFg) goto cleanup;
				/* 0.5 sec. per step */
				// Sleep(500*60); // i minutes per run
		// }
	// }

Submit01:
	/*
	K = ShellExecute(NULL, "open",
				Par2, NULL, NULL, SW_SHOWNORMAL); 
	if (K < (HINSTANCE)32) {
	*/
	K = system (SrvParm);
	if (K) {
			AddToMessageLog(TEXT("\nServiceStartError -- Submit Program Error.\n"),
							FALSE);
			goto cleanup;
	}

	if (!StopFg && i) goto Loop01;

cleanup:

    if (hServerStopEvent)
	{
		/*
        TerminateProcess(pi.hProcess, ExitCode);
        CloseHandle(pi.hThread);
        CloseHandle(pi.hProcess);
		*/
        CloseHandle(hServerStopEvent);
	}

	;
}
//
//  FUNCTION: ServiceStop
//
//  PURPOSE: Stops the service
//
//  PARAMETERS:
//    none
//
//  RETURN VALUE:
//    none
//
//  COMMENTS:
//    If a ServiceStop procedure is going to
//    take longer than 3 seconds to execute,
//    it should spawn a thread to execute the
//    stop code, and return.  Otherwise, the
//    ServiceControlManager will believe that
//    the service has stopped responding.
//    
VOID ServiceStop()
{
    if ( hServerStopEvent )
        if (bDebug)
	        AddToMessageLog(TEXT("\nServiceStop.\n"),
			                FALSE);

	   /*
       TerminateProcess(pi.hProcess, ExitCode);
       CloseHandle(pi.hThread);
       CloseHandle(pi.hProcess);
	   */

       SetEvent(hServerStopEvent);

       CloseHandle(hServerStopEvent);
	   StopFg = TRUE;
}
