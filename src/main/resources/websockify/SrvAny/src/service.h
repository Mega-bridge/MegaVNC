//
// Copyright Pedro P. Wong,  All Rights Reserved.
//
//  MODULE: service.h
//
//  need :
//  libcmt.lib kernel32.lib advapi.lib shell32.lib
//
//  This code also supports unicode.  Be sure to compile both service.c and
//  and code #include "service.h" with the same Unicode setting.
//
//  Note: This code also implements Ctrl+C and Ctrl+Break handlers
//        when using the debug option.
//
//        Also, this code only handles the OWN_SERVICE service type
//        running in the LOCAL_SYSTEM security context.
//
//        To control your service ( start, stop, etc ) you may use the
//        Services control panel applet or the NET.EXE program.
//
//        To aid in writing/debugging service, the
//        SDK contains a utility (MSTOOLS\BIN\SC.EXE) that
//        can be used to control, configure, or obtain service status.
//        SC displays complete status for any service/driver
//        in the service database, and allows any of the configuration
//        parameters to be easily changed at the command line.
//        For more information on SC.EXE, type SC at the command line.
//

#ifndef _SERVICE_H
#define _SERVICE_H

#ifdef __cplusplus
extern "C" {
#endif
//////////////////////////////////////////////////////////////////////////////
//// todo: change to desired strings
////
// name of the executable
#define SZAPPNAME            "SrvAny"
// internal name of the service
#define SZSERVICENAME        "SrvAnyService"
// displayed name of the service
#define SZSERVICEDISPLAYNAME "SrvAny Service"
// list of service dependencies - "dep1\0dep2\0\0"
#define SZDEPENDENCIES       ""
//////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
////       If a ServiceStop procedure is going to take longer than
////       3 seconds to execute, it should spawn a thread to
////       execute the stop code, and return.  Otherwise, the
////       ServiceControlManager will believe that the service has
////       stopped responding
////
VOID ServiceStart(DWORD dwArgc, LPTSTR *lpszArgv);
VOID ServiceStop();
//////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
//// The following are procedures which
//// may be useful to call within the above procedures,
//// but require no implementation by the user.
//// They are implemented in service.c

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
BOOL ReportStatusToSCMgr(DWORD dwCurrentState, DWORD dwWin32ExitCode, DWORD dwWaitHint);

//
//  FUNCTION: AddToMessageLog(LPTSTR lpszMsg, BOOL Eflag)
//
//  PURPOSE: Allows any thread to log an error message
//
//  PARAMETERS:
//    lpszMsg - text for message
//    Eflag - error(TRUE) or information meaasge
//
//  RETURN VALUE:
//    none
//
void AddToMessageLog(LPTSTR lpszMsg, BOOL Eflag);
//////////////////////////////////////////////////////////////////////////////

#ifdef __cplusplus
}
#endif

#endif
