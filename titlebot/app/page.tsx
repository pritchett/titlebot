"use client";
import { useState, useEffect} from "react"

const Title = (props: { title: string }) => {
  if (props && props.title) {
    return <div>{props.title}</div>
  }
  return <></>
}

const Favicon = (props: { favicon: string }) => {
  if (props && props.favicon) {
    return <img src={`data:image/jpeg;base64,${props.favicon}`} />
  }

  return <></>
}

const TitleFavicon = (props: { title: string, favicon: string }) => {
    return (
    <div className="p-4">
      <Favicon favicon={props.favicon} />
      <Title title={props.title} />
    </div>
    )

  return <></>
}

export default function Home() {
  const [url, setUrl] = useState("https://google.com")
  const [data, setData] = useState({title: "", favicon: ""})

  const handleChange = (event: any) => {
    setUrl(event.target.value)
  }

  const handleSubmit = (event: any) => {
    event.preventDefault();
    fetch(`http://localhost:8080/title_favicon?uri=${url}`)
      .then((response) => response.json())
      .then((data) => setData(data))
      .catch((err) => alert(err))
  }
  return (
    <main className="flex min-h-screen flex-col items-center p-24">
      <form className="w-full max-w-lg" onSubmit={handleSubmit}>
        <div>
            <label className="">Enter a URL:
            <input
              className="p-2"
              type="text"
              name="url"
              defaultValue="https://google.com"
              onChange={handleChange}
            /> 
          </label>
            <button type="submit" className="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800">Submit</button>
        </div>
      </form>
      <div className="flex">
        <TitleFavicon title={data.title} favicon={data.favicon} />
      </div>
    </main>
  );
}
