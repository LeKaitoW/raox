#!/usr/bin/python

import argparse
import ftplib
import re
from sets import Set


class VersionedFile:
	def __init__(self, file_name, file_version):
		self.file_name = file_name
		self.file_version = file_version

	def __str__(self):
		return self.file_name

	def __repr__(self):
		return self.__str__()


def connectFtp(login, password):
	ftp = ftplib.FTP('rk9.bmstu.ru')
	ftp.login(login, password)
	return ftp


def getFtpFiles(ftp, directory):
	ftp.cwd(directory)
	files = []
	ftp.retrlines('MLSD', files.append)
	return files


def removeFtpFiles(ftp, files):
	for file in files:
		ftp.delete(file)


def parseFtpFiles(files):
	rao_eclipse_files = []
	rao_plugins_files = []
	other_files = []

	for file in files:
		file_info = file.split(';')
		file_type = file_info[0].split('=')[1]
		if file_type != 'file':
			continue
		file_name = file_info[3].strip()
		rao_match = re.search('rao-(\d+\.\d+\.\d+)-(win|linux|mac|plugins).*', file_name)
		if rao_match:
			if rao_match.group(2) == 'plugins':
				rao_plugins_files.append(VersionedFile(file_name = file_name, file_version = rao_match.group(1)))
			else:
				rao_eclipse_files.append(VersionedFile(file_name = file_name, file_version = rao_match.group(1)))
		else:
			other_files.append(file_name)

	return rao_eclipse_files, rao_plugins_files, other_files


def getVersions(versioned_files):
	unsorted_unique_versions = Set()
	for file in versioned_files:
		unsorted_unique_versions.add(file.file_version)
	sorted_versions = []
	for unsorted_unique_version in unsorted_unique_versions:
		sorted_versions.append(unsorted_unique_version)
	sorted_versions.sort(reverse = True)
	return sorted_versions


def removeVersionedFtpFile(ftp, versioned_files, versions_limit):
	versions = getVersions(versioned_files)
	if versions_limit < len(versions):
		files_to_remove = []
		for file in versioned_files:
			if file.file_version < versions[versions_limit-1]:
				files_to_remove.append(file.file_name)
		removeFtpFiles(ftp, files_to_remove)


if __name__ == "__main__":
	argument_parser = argparse.ArgumentParser()
	argument_parser.add_argument('--login', help='ftp login', default='')
	argument_parser.add_argument('--password', help='ftp password', default='')
	argument_parser.add_argument('--directory', help='ftp directory', default='tmp')
	argument_parser.add_argument('--versions_limit', type=int, help="files last versions limit count", default=5)
	args = argument_parser.parse_args()

	ftp = connectFtp(args.login, args.password)
	rao_eclipse_files, rao_plugins_files, other_files = parseFtpFiles(getFtpFiles(ftp, args.directory))

	removeVersionedFtpFile(ftp, rao_eclipse_files, args.versions_limit)
	removeVersionedFtpFile(ftp, rao_plugins_files, args.versions_limit)
	removeFtpFiles(ftp, other_files)
