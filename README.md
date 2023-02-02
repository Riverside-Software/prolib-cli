## Description

Command line utility to extract PL content 

## Requirements

Java 11

## List command line

```
java -jar prolib-cli.jar list /path/to/pl
```

Sample output:
```
   CRC             Timestamp                                        Digest        Size  File name
 53261  2022-03-26T04:01:23Z  QTXjoxXYhuUTSP4SwQphzHZLoObQ+zocwp6G07tChOc=       40649  adecomm/as-utils.r
 46105  2022-03-26T04:01:23Z  yuWGXRthulcthS63h+wSYvoFTTrncb9Q/+c0uKNPmno=        6162  adecomm/convcp.r
 59355  2022-03-26T04:01:23Z  2/UTLWjNqrIG0uF02ccge4dk5HQYoLJMAR4uT4hEH+w=        1578  adecomm/get-user.r
 10006  2022-03-26T04:01:26Z  1buNZtflScmTUH3bbU/anvph3eIUTRehbUBu4eJmGpg=        1036  adecomm/ide_chscolr.r
 34197  2022-03-26T04:01:24Z  G9sREqmLEItpPUmE9uAc9achtXYG1zml9ZcTkY8QzZs=      170828  adecomm/oeideservice.r
 ```

## Scan command line

```
java -jar prolib-cli.jar extract /path/to/pl
```

Extract PL file in current directory

## Compare command line

```
java -jar prolib-cli.jar compare /path/to/pl1 /path/to/pl2
```

Show differences between two PL files.
