import ftplib
import re
import versioned_file

HOST = 'rk9.bmstu.ru'


def connect(login, password):
    ftp = ftplib.FTP(HOST)
    ftp.login(login, password)
    return ftp


def get_files(ftp, directory):
    ftp.cwd(directory)
    files = []
    ftp.retrlines('MLSD', files.append)
    return files


def remove_files(ftp, file_names):
    for file_name in file_names:
        ftp.delete(file_name)


def parse_files(ftp_file_infos):
    rao_eclipse_files = []
    rao_plugins_files = []
    other_files = []

    for ftp_file_info in ftp_file_infos:
        ftp_file_info = ftp_file_info.split(';')
        file_type = ftp_file_info[0].split('=')[1]
        if file_type != 'file':
            continue
        file_name = ftp_file_info[3].strip()
        rao_match = re.search('rao-(\d+\.\d+\.\d+)-(win|linux|mac|plugins).*', file_name)
        if rao_match:
            if rao_match.group(2) == 'plugins':
                rao_plugins_files.append(
                    versioned_file.VersionedFile(file_name=file_name, file_version=rao_match.group(1)))
            else:
                rao_eclipse_files.append(
                    versioned_file.VersionedFile(file_name=file_name, file_version=rao_match.group(1)))
        else:
            other_files.append(file_name)

    return rao_eclipse_files, rao_plugins_files, other_files


def remove_versioned_file(ftp, versioned_files, versions_limit):
    files_to_remove = versioned_file.get_outdated_files(versioned_files, versions_limit)
    remove_files(ftp, files_to_remove)
