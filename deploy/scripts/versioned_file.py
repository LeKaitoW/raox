from sets import Set


class VersionedFile:
	def __init__(self, file_name, file_version):
		self.file_name = file_name
		self.file_version = file_version

	def __str__(self):
		return self.file_name

	def __repr__(self):
		return self.__str__()


def getSortedVersions(versioned_files):
	unsorted_unique_versions = Set()
	for file in versioned_files:
		unsorted_unique_versions.add(file.file_version)
	sorted_versions = []
	for unsorted_unique_version in unsorted_unique_versions:
		sorted_versions.append(unsorted_unique_version)
	sorted_versions.sort(reverse = True)
	return sorted_versions


def getOutdatedFiles(versioned_files, versions_limit):
	sorted_versions = getSortedVersions(versioned_files)
	files = []
	if versions_limit < len(sorted_versions):
		files_to_remove = []
		for file in versioned_files:
			if file.file_version < sorted_versions[versions_limit-1]:
				files.append(file.file_name)
	return files
