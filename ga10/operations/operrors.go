package operations

import(
	"errors"
)

var (
	ErrorItemIDIncluded = errors.New("ItemID included")
	ErrorDatabaseError = errors.New("Database Error")

	ErrorItemNotUpdated = errors.New("Item not updated")
	ErrorItemNotFound = errors.New("Item not found")
	ErrorItemNotDeleted = errors.New("Element not deleted")


)