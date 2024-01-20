package utilities

import(
        "github.com/google/uuid"
)


func MakeID() string {
	return uuid.New().String()
}
