import ftplib
import re
import versioned_file


HOST='rk9.bmstu.ru'


def connect(login, password):
	ftp = ftplib.FTP(HOST)
	ftp.login(login, password)
	return ftp


def getFiles(ftp, directory):
	ftp.cwd(directory)
	files = []
	ftp.retrlines('MLSD', files.append)
	return files


def removeFiles(ftp, files):
	for file in files:
		ftp.delete(file)


def parseFiles(files):
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
				rao_plugins_files.append(versioned_file.VersionedFile(file_name = file_name, file_version = rao_match.group(1)))
			else:
				rao_eclipse_files.append(versioned_file.VersionedFile(file_name = file_name, file_version = rao_match.group(1)))
		else:
			other_files.append(file_name)

	return rao_eclipse_files, rao_plugins_files, other_files


def removeVersionedFile(ftp, versioned_files, versions_limit):
	files_to_remove = versioned_file.getOutdatedFiles(versioned_files, versions_limit)
	removeFiles(ftp, files_to_remove)
