;(function () {
  function customizeTopNavbar() {
    var navbarEnd = document.querySelector('#topbar-nav .navbar-end')
    if (!navbarEnd) return

    var navContainer = document.querySelector('.nav-container')
    var docVersion = navContainer && navContainer.dataset ? navContainer.dataset.version : null
    var version = docVersion || 'latest'

    var titleLink = document.querySelector('.nav-menu .title a')
    var homeHref = titleLink ? titleLink.getAttribute('href') : 'index.html'
    var homeUrl = new URL(homeHref, window.location.href)
    var usageUrl = new URL('usage/index.html', homeUrl)
    var developmentUrl = new URL('development/index.html', homeUrl)
    var downloadUrl = 'https://registry.nextflow.io/plugins/nf-bids@' + encodeURIComponent(version)

    navbarEnd.innerHTML = ''

    var homeItem = document.createElement('a')
    homeItem.className = 'navbar-item'
    homeItem.href = homeUrl.href
    homeItem.textContent = 'Home'

    var usageItem = document.createElement('a')
    usageItem.className = 'navbar-item'
    usageItem.href = usageUrl.href
    usageItem.textContent = 'Usage'

    var developmentItem = document.createElement('a')
    developmentItem.className = 'navbar-item'
    developmentItem.href = developmentUrl.href
    developmentItem.textContent = 'Development'

    var downloadWrapper = document.createElement('div')
    downloadWrapper.className = 'navbar-item'
    var control = document.createElement('span')
    control.className = 'control'
    var downloadButton = document.createElement('a')
    downloadButton.className = 'button is-primary'
    downloadButton.href = downloadUrl
    downloadButton.textContent = 'Download'
    control.appendChild(downloadButton)
    downloadWrapper.appendChild(control)

    navbarEnd.appendChild(homeItem)
    navbarEnd.appendChild(usageItem)
    navbarEnd.appendChild(developmentItem)
    navbarEnd.appendChild(downloadWrapper)
  }

  function makeImagesClickable() {
    var images = document.querySelectorAll('.doc .imageblock img')
    images.forEach(function (img) {
      if (img.closest('a')) return
      var src = img.getAttribute('src')
      if (!src) return
      var link = document.createElement('a')
      link.className = 'nf-bids-fullres-link'
      link.href = src
      link.target = '_blank'
      link.rel = 'noopener noreferrer'
      img.parentNode.insertBefore(link, img)
      link.appendChild(img)
    })
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () {
      customizeTopNavbar()
      makeImagesClickable()
    })
  } else {
    customizeTopNavbar()
    makeImagesClickable()
  }
})()
