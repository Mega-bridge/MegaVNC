# Microsoft Developer Studio Generated NMAKE File, Based on srvany.dsp
!IF "$(CFG)" == ""
CFG=srvany - Win32 Debug
!MESSAGE No configuration specified. Defaulting to srvany - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "srvany - Win32 Release" && "$(CFG)" != "srvany - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "srvany.mak" CFG="srvany - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "srvany - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE "srvany - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

!IF  "$(CFG)" == "srvany - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\srvany.exe"


CLEAN :
	-@erase "$(INTDIR)\service.obj"
	-@erase "$(INTDIR)\srvany.obj"
	-@erase "$(INTDIR)\SrvAny.res"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(OUTDIR)\srvany.exe"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /ML /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /Fp"$(INTDIR)\srvany.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

.c{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.c{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

RSC=rc.exe
RSC_PROJ=/l 0x404 /fo"$(INTDIR)\SrvAny.res" /d "NDEBUG" 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\srvany.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /incremental:no /pdb:"$(OUTDIR)\srvany.pdb" /machine:I386 /out:"$(OUTDIR)\srvany.exe" 
LINK32_OBJS= \
	"$(INTDIR)\service.obj" \
	"$(INTDIR)\srvany.obj" \
	"$(INTDIR)\SrvAny.res"

"$(OUTDIR)\srvany.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "srvany - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\srvany.exe"


CLEAN :
	-@erase "$(INTDIR)\service.obj"
	-@erase "$(INTDIR)\srvany.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\srvany.exe"
	-@erase "$(OUTDIR)\srvany.ilk"
	-@erase "$(OUTDIR)\srvany.pdb"
	-@erase ".\SrvAny.res"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MLd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /Fp"$(INTDIR)\srvany.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 

.c{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.c{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

RSC=rc.exe
RSC_PROJ=/l 0x404 /fo"SrvAny.res" /d "_DEBUG" 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\SrvAny.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /incremental:yes /pdb:"$(OUTDIR)\SrvAny.pdb" /debug /machine:I386 /out:"$(OUTDIR)\SrvAny.exe" /pdbtype:sept 
LINK32_OBJS= \
	"$(INTDIR)\service.obj" \
	"$(INTDIR)\srvany.obj" \
	".\SrvAny.res"

"$(OUTDIR)\srvany.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF 


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("srvany.dep")
!INCLUDE "srvany.dep"
!ELSE 
!MESSAGE Warning: cannot find "srvany.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "srvany - Win32 Release" || "$(CFG)" == "srvany - Win32 Debug"
SOURCE=.\service.c

"$(INTDIR)\service.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\srvany.c

"$(INTDIR)\srvany.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\SrvAny.rc

!IF  "$(CFG)" == "srvany - Win32 Release"


"$(INTDIR)\SrvAny.res" : $(SOURCE) "$(INTDIR)"
	$(RSC) $(RSC_PROJ) $(SOURCE)


!ELSEIF  "$(CFG)" == "srvany - Win32 Debug"


".\SrvAny.res" : $(SOURCE) "$(INTDIR)"
	$(RSC) $(RSC_PROJ) $(SOURCE)


!ENDIF 


!ENDIF 

