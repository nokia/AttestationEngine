const doFetch = async (url: string, options?: any) => {
  const response = await fetch(url, options)
  const json = await response.json()
  if (json.error) {
    throw new Error(json.message + ': ' + json.error)
  } else if (!response.ok) {
    throw new Error('Something went wrong fetching')
  } else {
    return json
  }
}

export {doFetch}
