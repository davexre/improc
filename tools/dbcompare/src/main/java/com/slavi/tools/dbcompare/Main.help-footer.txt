Examples:
  dbcompare -f data.sqlite -su 'scott' -sp 'tiger' -s 'localhost:1521:XE'

  dbcompare -f data.sqlite -tu 'scott' -tp 'tiger' -t '(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=localhost)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=XE)))'

  dbcompare -f data.sqlite -c
