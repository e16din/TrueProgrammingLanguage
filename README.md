### True Programming Language | Version: Prototype

Я заметил что сподручнее писать код когда ты что-то создаешь, а затем только называешь. И подумал что было бы круто иметь инструммент помогающий в этом. 

Пришла идея языка программирования по этому принципу, набросал простую реализацию ЯП с транслятором в Kotlin, получилось довольно интересно.


```kotlin
Программ на языке True:

// NOTE: сначала что-то создаю, затем называю
{ 
    10 = pageSize
    20L = maxPages
    "Ok" = okText
    "Cancel" = cancelText
    
    false -> main()
    //
    
    {
      empty = text : String
    } = Button

    Button(okText) = okButton
    true = isVisible
    1, 2, 3 = ads
    ads.size > 3 -> show(okButton, isVisible) = funcName
    
} = main

Транслируем ее в Kotlin:

// NOTE: сначала что-то создаю, затем называю
fun main() {
	var pageSize = 10
	var maxPages = 20L
	var okText = "Ok"
	var cancelText = "Cancel"

	if (false) {
		main()
	}
    //

	fun Button(text: String) {
	}

	var okButton = Button(okText)
	var isVisible = true
	var ads = mutableListOf(1,2,3)
	fun funcName() {
		if (ads.size>3) {
			show(okButton,isVisible)
		}
	}

}
```
