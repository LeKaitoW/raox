#!/bin/bash

thirdparty_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

download_library() {
	local download_path=$1
	local destination_path=$2

	echo download $download_path
	wget -q "$download_path" -O "$destination_path"

	if [ $? -ne 0 ]; then
		echo "failed to download $2"
		exit 1
	fi
}

create_output_folders() {
	local library_name=$1

	rm -rf "$library_name"
	mkdir -p "$library_name"
	mkdir -p "$library_name"/lib
	mkdir -p "$library_name"/source
}

unpack_source() {
	local archive_name=$1
	local library_name=$2
	local source=$3
	local output_folder=$4

	unzip "$archive_name" "$library_name"/"$source"/* \
			-d "$output_folder" >/dev/null
	cd "$output_folder"/"$library_name"/ >/dev/null
	zip -r "$output_folder"/source/"$source".zip \
			"$source"/ >/dev/null
	cd - >/dev/null
}

get_jfreechart_library() {
	echo jfreechart
	local download_path="http://sourceforge.net/projects/jfreechart/files/latest/download?source=files"
	local library_name=jfreechart-1.0.19
	local archive_name="$thirdparty_dir"/"$library_name".zip
	local output_folder="$thirdparty_dir"/jfreechart

	download_library "$download_path" "$archive_name"
	echo generate $output_folder
	create_output_folders "$output_folder"

	declare -a sources=(source swt experimental)

	for source in "${sources[@]}"
	do
		unpack_source "$archive_name" "$library_name" "$source" "$output_folder"
	done

	declare -a libs=(jcommon-1.0.23.jar jfreechart-1.0.19.jar jfreechart-1.0.19-experimental.jar jfreechart-1.0.19-swt.jar)

	for lib in "${libs[@]}"
	do
		unzip -j "$archive_name" "$library_name"/lib/"$lib" \
			-d "$output_folder"/lib >/dev/null
	done

	rm -rf "$output_folder"/"$library_name"
	rm "$archive_name"
	echo done
}

get_jgraphx_library() {
	echo jgraphx
	local download_path=https://github.com/jgraph/jgraphx/archive/v3.3.1.1.zip
	local library_name=jgraphx-3.3.1.1
	local archive_name="$thirdparty_dir"/"$library_name".zip
	local output_folder="$thirdparty_dir"/jgraphx

	download_library "$download_path" "$archive_name"
	echo generate $output_folder
	create_output_folders "$output_folder"

	unpack_source "$archive_name" "$library_name" src "$output_folder"

	unzip -j "$archive_name" "$library_name"/lib/jgraphx.jar \
		-d "$output_folder"/lib >/dev/null

	rm -rf "$output_folder"/"$library_name"
	rm "$archive_name"
	echo done
}

get_jfreechart_library
get_jgraphx_library
