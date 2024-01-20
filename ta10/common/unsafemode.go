package utilities


var unsafemode bool = false

func SetUnsafeMode() {
	unsafemode = true
}

func IsUnsafe() bool {
	return unsafemode
}