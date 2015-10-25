#!/usr/bin/python
# -*- coding: utf-8 -*-

import argparse
import ftp_server


HOST='rdo.rk9.bmstu.ru'


def getSortedVersionedFiles(versioned_files):
	return sorted(versioned_files, key=lambda versioned_file: versioned_file.file_version, reverse=True)


def generateDownloadPage(versioned_files, directory, program_name):
	if not versioned_files:
		return ''
		
	page = '<P><B>{program_name}</B></P>'.format(program_name = program_name)
	last_version = ''
	for file in versioned_files:
		if last_version != file.file_version:
			last_version = file.file_version
			page += '<P>{last_version}</P>\n'.format(last_version = last_version)
		page += '<A HREF="http://{host}/{directory}/{file_name}">{file_name}</A><BR>\n'.format(host = HOST, directory = directory, file_name = file.file_name)
	return page


if __name__ == "__main__":
	argument_parser = argparse.ArgumentParser()
	argument_parser.add_argument('--login', help='ftp login', default='')
	argument_parser.add_argument('--password', help='ftp password', default='')
	argument_parser.add_argument('--directory', help='ftp directory', default='tmp')
	args = argument_parser.parse_args()

	ftp = ftp_server.connect(args.login, args.password)
	rao_eclipse_files, rao_plugins_files, other_files = ftp_server.parseFiles(ftp_server.getFiles(ftp, args.directory))
	rao_eclipse_files = getSortedVersionedFiles(rao_eclipse_files)
	rao_plugins_files = getSortedVersionedFiles(rao_plugins_files)

	page = '''
<HTML>
<HEAD>
<TITLE>Язык имитационного моделирования РДО</TITLE>
<META http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK rel = "stylesheet" type = "text/css" href = "shared/styles/styles.css">
</HEAD>
<BODY>
<H1>Загрузить</H1>
<TABLE cols="2" cellspacing="10" cellpadding="10">
<TR valign="top">
'''
	page += '<TD>' + generateDownloadPage(rao_eclipse_files, args.directory, 'RAO Eclipse') + '</TD>'
	page += '<TD>' + generateDownloadPage(rao_plugins_files, args.directory, 'RAO Plugin') + '</TD>'
	page += '''
</TR>
</TABLE>
</BODY>
'''
	print page
