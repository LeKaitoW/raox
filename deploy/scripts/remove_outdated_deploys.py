#!/usr/bin/env python

import argparse
import ftp_server

if __name__ == "__main__":
    argument_parser = argparse.ArgumentParser()
    argument_parser.add_argument('--login', help='ftp login', default='')
    argument_parser.add_argument('--password', help='ftp password', default='')
    argument_parser.add_argument('--directory', help='ftp directory', default='tmp')
    argument_parser.add_argument('--versions_limit', type=int, help="number of last versions to store", default=5)
    args = argument_parser.parse_args()

    ftp = ftp_server.connect(args.login, args.password)
    raox_eclipse_files, raox_files, raox_game5_files, other_files = ftp_server.parse_files(ftp_server.get_files(ftp, args.directory))

    ftp_server.remove_versioned_file(ftp, raox_eclipse_files, args.versions_limit)
    ftp_server.remove_versioned_file(ftp, raox_files, args.versions_limit)
    ftp_server.remove_versioned_file(ftp, raox_game5_files, args.versions_limit)
    ftp_server.remove_files(ftp, other_files)
