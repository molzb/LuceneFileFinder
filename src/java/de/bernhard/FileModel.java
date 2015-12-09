package de.bernhard;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class FileModel {
	public static final DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	@Getter
	private final String name;
	@Getter
	private final long size;
	@Getter
	private final boolean isDir;
	@Getter
	private final boolean isHidden;
	@Getter
	private final String path;
	@Getter
	private final Date modified;
	@Getter
	private final String lastModifiedStr;
}
