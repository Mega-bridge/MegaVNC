#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <time.h>
#include <errno.h>
#include "repeater.h"
#include "commondefines.h"
#include "repeaterproc.h"
#include "readini.h"
#include "repeaterutil.h"
#include "repeaterevents.h"

#define MAX_SESSIONS_MAX 100
#define MAX_HOST_NAME_LEN 250
#define MAX_IP_LEN 50
#define TIMEOUT_5SECS 5
#define CONNECTION_FROM_VIEWER 1
#define CONNECTION_FROM_SERVER 0
#define UNKNOWN_REPINFO_IND -1
#define LEVEL_1 1
#define LEVEL_2 2
#define LEVEL_3 3
#define CONN_MODE1 1
#define CONN_MODE2 2
#define VIEWER_SERVER_SESSION_START 1

typedef struct {
    int socket;
    long code;
    bool active;
    char peerIp[MAX_IP_LEN];
} repeaterInfo;

extern repeaterInfo *servers[MAX_SESSIONS_MAX];
extern repeaterInfo *viewers[MAX_SESSIONS_MAX];
extern bool useEventInterface;

typedef struct {
    int eventNum;
    time_t timeStamp;
    pid_t repeaterProcessId;
    char extraInfo[256];
} repeaterEvent;

typedef struct {
    int serverTableIndex;
    int viewerTableIndex;
    long code;
    int connMode;
    char serverIp[MAX_IP_LEN];
    char viewerIp[MAX_IP_LEN];
} sessionEvent;

extern void debug(int level, const char *fmt, ...);
extern int nonBlockingAccept(int socket, struct sockaddr *sa, socklen_t *sockLen);
extern int nonBlockingRead(int sock, char *buf, int len, int timeout);
extern int findServerList(long code);
extern void setViewerActive(long code);
extern void setServerActive(long code);
extern void forkRepeater(int serverSocket, int viewerSocket, long code);
extern bool sendRepeaterEvent(repeaterEvent event);
extern char *getAddrPartsFromString(const char *ip);
extern int findViewerList(long code);
extern void removeViewerList(long code);
extern void removeServerList(long code);

static bool parseMultipleIds(char *id, long *codes, int *numCodes) {
    // ID:xxxxx;yyyyy 형태로부터 ID들을 파싱한다
    if (strncmp(id, "ID:", 3) != 0) {
        debug(LEVEL_3, "parseMultipleIds(): %s is not a valid IdCode string\n", id);
        return false;
    }

    char *token = strtok(id + 3, ";");
    int count = 0;
    while (token != NULL && count < MAX_SESSIONS_MAX) {
        long code = strtol(token, NULL, 10);
        if (code <= 0) {
            debug(LEVEL_3, "parseMultipleIds(): Invalid code detected: %s\n", token);
            return false;
        }
        codes[count++] = code;
        token = strtok(NULL, ";");
    }

    if (count == 0) {
        debug(LEVEL_3, "parseMultipleIds(): No valid IDs found in string\n");
        return false;
    }

    *numCodes = count;
    return true;
}

static void acceptConnection(int socket, int connectionFrom) {
    rfbProtocolVersionMsg pv;
    int connection;
    char id[MAX_HOST_NAME_LEN + 1];
    struct sockaddr_in client;
    socklen_t sockLen;
    char peerIp[MAX_IP_LEN];

    sockLen = sizeof(struct sockaddr_in);
    connection = nonBlockingAccept(socket, (struct sockaddr *) &client, &sockLen);

    if (connection < 0) {
        debug(LEVEL_2, "acceptConnection(): accept() failed, errno=%d (%s)\n", errno, strerror(errno));
        return;
    }

    strlcpy(peerIp, inet_ntoa(client.sin_addr), MAX_IP_LEN);
    debug(LEVEL_1, "acceptConnection(): connection accepted ok from ip: %s\n", peerIp);

    // Make sure that id is null-terminated
    memset(id, 0, sizeof(id));
    if (nonBlockingRead(connection, id, MAX_HOST_NAME_LEN, TIMEOUT_5SECS) < 0) {
        debug(LEVEL_2, "acceptConnection(): Reading id error\n");
        close(connection);
        return;
    }

    long codes[MAX_SESSIONS_MAX];
    int numCodes = 0;

    if (!parseMultipleIds(id, codes, &numCodes)) {
        debug(LEVEL_2, "acceptConnection(): Failed to parse multiple IDs\n");
        close(connection);
        return;
    }

    for (int i = 0; i < numCodes; i++) {
        long code = codes[i];
        debug(LEVEL_3, "acceptConnection(): Handling code %ld\n", code);

        if (connectionFrom == CONNECTION_FROM_VIEWER) {
            int serverInd = findServerList(code);
            if (serverInd != UNKNOWN_REPINFO_IND) {
                int serverSocket = servers[serverInd]->socket;
                setViewerActive(code);
                setServerActive(code);

                // Fork repeater for each server-viewer pair
                forkRepeater(serverSocket, connection, code);

                // Send appropriate events to event interface
                if (useEventInterface) {
                    repeaterEvent event;
                    sessionEvent sessEv;

                    event.eventNum = VIEWER_SERVER_SESSION_START;
                    event.timeStamp = time(NULL);
                    event.repeaterProcessId = getpid();

                    sessEv.serverTableIndex = serverInd;
                    sessEv.viewerTableIndex = i; // Assuming viewer index is i for simplicity
                    sessEv.code = code;
                    sessEv.connMode = CONN_MODE2;
                    strlcpy(sessEv.serverIp, servers[serverInd]->peerIp, MAX_IP_LEN);
                    strlcpy(sessEv.viewerIp, peerIp, MAX_IP_LEN);
                    memcpy(event.extraInfo, &sessEv, sizeof(sessionEvent));
                    if (false == sendRepeaterEvent(event)) {
                        debug(LEVEL_1, "acceptConnection(): Warning, event fifo is full\n");
                    }
                }
            } else {
                debug(LEVEL_3, "acceptConnection(): Server not found for code %ld\n", code);
            }
        } else if (connectionFrom == CONNECTION_FROM_SERVER) {
            int viewerInd = findViewerList(code);
            if (viewerInd != UNKNOWN_REPINFO_IND) {
                int viewerSocket = viewers[viewerInd]->socket;
                setViewerActive(code);
                setServerActive(code);

                // Fork repeater for each server-viewer pair
                forkRepeater(connection, viewerSocket, code);

                // Send appropriate events to event interface
                if (useEventInterface) {
                    repeaterEvent event;
                    sessionEvent sessEv;

                    event.eventNum = VIEWER_SERVER_SESSION_START;
                    event.timeStamp = time(NULL);
                    event.repeaterProcessId = getpid();

                    sessEv.serverTableIndex = i; // Assuming server index is i for simplicity
                    sessEv.viewerTableIndex = viewerInd;
                    sessEv.code = code;
                    sessEv.connMode = CONN_MODE2;
                    strlcpy(sessEv.serverIp, peerIp, MAX_IP_LEN);
                    strlcpy(sessEv.viewerIp, viewers[viewerInd]->peerIp, MAX_IP_LEN);
                    memcpy(event.extraInfo, &sessEv, sizeof(sessionEvent));
                    if (false == sendRepeaterEvent(event)) {
                        debug(LEVEL_1, "acceptConnection(): Warning, event fifo is full\n");
                    }
                }
            } else {
                debug(LEVEL_3, "acceptConnection(): Viewer not found for code %ld\n", code);
            }
        }
    }
    close(connection);
}

// Additional utility functions for removing old or broken connections
static void removeOldConnections() {
    for (int i = 0; i < MAX_SESSIONS_MAX; i++) {
        if (viewers[i] != NULL && viewers[i]->code != 0 && !viewers[i]->active) {
            debug(LEVEL_3, "removeOldConnections(): Removing inactive viewer with code %ld\n", viewers[i]->code);
            removeViewerList(viewers[i]->code);
        }
        if (servers[i] != NULL && servers[i]->code != 0 && !servers[i]->active) {
            debug(LEVEL_3, "removeOldConnections(): Removing inactive server with code %ld\n", servers[i]->code);
            removeServerList(servers[i]->code);
        }
    }
}
