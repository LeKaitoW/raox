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
    raox_eclipse_files = []
    raox_files = []
    raox_game5_files = []
    other_files = []

    for ftp_file_info in ftp_file_infos:
        ftp_file_info = ftp_file_info.split(';')
        file_type = ftp_file_info[0].split('=')[1]
        if file_type != 'file':
            continue
        file_name = ftp_file_info[3].strip()
        raox_match = re.search('raox(-(eclipse|game5))?-(\d+\.\d+\.\d+)(\.(\d+-g[0-9a-f]+))?(-(win|linux|mac))?.*', file_name)
        if raox_match:
            long_git_version = raox_match.group(5)
            if not long_git_version:
                file_version = raox_match.group(3)
                if raox_match.group(2) == 'eclipse':
                    raox_eclipse_files.append(
                        versioned_file.VersionedFile(file_name=file_name, file_version=file_version))
                elif raox_match.group(2) == 'game5':
                    raox_game5_files.append(
                        versioned_file.VersionedFile(file_name=file_name, file_version=file_version))
                else:
                    raox_files.append(
                        versioned_file.VersionedFile(file_name=file_name, file_version=file_version))
            else:
                other_files.append(file_name)
        else:
            other_files.append(file_name)

    return raox_eclipse_files, raox_files, raox_game5_files, other_files


def remove_versioned_file(ftp, versioned_files, versions_limit):
    files_to_remove = versioned_file.get_outdated_files(versioned_files, versions_limit)
    remove_files(ftp, files_to_remove)
