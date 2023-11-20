#ifndef __YOTTA_BUILD_INFO_H__
#define __YOTTA_BUILD_INFO_H__
// yotta build info, #include YOTTA_BUILD_INFO_HEADER to access
#define YOTTA_BUILD_YEAR 2023 // UTC year
#define YOTTA_BUILD_MONTH 10 // UTC month 1-12
#define YOTTA_BUILD_DAY 27 // UTC day 1-31
#define YOTTA_BUILD_HOUR 11 // UTC hour 0-24
#define YOTTA_BUILD_MINUTE 50 // UTC minute 0-59
#define YOTTA_BUILD_SECOND 40 // UTC second 0-61
#define YOTTA_BUILD_UUID a69069b1-3466-43bb-9169-5641b1fbcfba // unique random UUID for each build
#define YOTTA_BUILD_VCS_ID b'da6dadb556b122f9ecbbce7aa05c5c0190886583' // git or mercurial hash
#define YOTTA_BUILD_VCS_CLEAN 0 // evaluates true if the version control system was clean, otherwise false
#define YOTTA_BUILD_VCS_DESCRIPTION b'da6dadb' // git describe or mercurial equivalent
#endif // ndef __YOTTA_BUILD_INFO_H__
