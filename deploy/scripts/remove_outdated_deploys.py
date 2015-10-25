#!/usr/bin/python

import argparse
import ftp_server


if __name__ == "__main__":
	argument_parser = argparse.ArgumentParser()
	argument_parser.add_argument('--login', help='ftp login', default='')
	argument_parser.add_argument('--password', help='ftp password', default='')
	argument_parser.add_argument('--directory', help='ftp directory', default='tmp')
	argument_parser.add_argument('--versions_limit', type=int, help="files last versions limit count", default=5)
	args = argument_parser.parse_args()

	ftp = ftp_server.connect(args.login, args.password)
	rao_eclipse_files, rao_plugins_files, other_files = ftp_server.parseFiles(ftp_server.getFiles(ftp, args.directory))

	ftp_server.removeVersionedFile(ftp, rao_eclipse_files, args.versions_limit)
	ftp_server.removeVersionedFile(ftp, rao_plugins_files, args.versions_limit)
	ftp_server.removeFiles(ftp, other_files)
