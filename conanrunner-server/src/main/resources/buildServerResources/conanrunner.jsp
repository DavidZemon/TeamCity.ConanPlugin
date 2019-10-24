<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="commonStrings" class="net.redlion.ci.ConanConstantsBean"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<%--suppress ES6ConvertVarToLetConst --%>
<script>
    BS.RLC = {
        initPage: function () {
        },

        onDockerImageUpdate: function (value, classToToggle) {
            var el = $j('.' + classToToggle);
            if (value) {
                if (el.hasClass('hidden')) {
                    el.removeClass('hidden');
                    this._scaleDown(el, 150);
                    setTimeout(function () {
                        BS.MultilineProperties.updateVisible();
                    }, 150);
                }
            } else {
                el.addClass('hidden');
            }
        },
        _scaleDown: function (el, msecs) {
            if (typeof el[0].style.transform !== 'undefined') {
                el.css('transformOrigin', '0 0');
                var start = 0;

                function step(timestamp) {
                    if (!start) {
                        start = timestamp;
                    }

                    var progress = (timestamp - start) / msecs;
                    if (progress <= 1) {
                        el.css("transform", 'scaleY(' + progress + ')');
                        window.requestAnimationFrame(step);
                    } else {
                        el.css("transform", 'none');
                    }
                }

                window.requestAnimationFrame(step);
            }
        }
    };
</script>

<%-- Conan path --%>
<tr class="advancedSetting">
    <th><label for="${commonStrings.conanCommandKey}">Conan program path: </label></th>
    <td>
        <props:textProperty name="${commonStrings.conanCommandKey}" className="longField" maxlength="512"/>
        <span class="smallNote">Enter path to Conan program or leave blank for using default.</span>
    </td>
</tr>

<%-- Working directory --%>
<forms:workingDirectory/>

<%-- Recipe path --%>
<tr class="advancedSetting">
    <th><label for="${commonStrings.conanRecipePathKey}">Path to recipe
        (<code>conanfile.py</code>/<code>conanfile.txt</code>): </label></th>
    <td>
        <props:textProperty name="${commonStrings.conanRecipePathKey}" className="longField" maxlength="512"/>
        <span class="smallNote">Enter path to Conan recipe relative to working directory or leave blank to use '.' .</span>
    </td>
</tr>

<%-- Conan user --%>
<tr>
    <th><label for="${commonStrings.conanUserKey}">Conan user: <l:star/></label></th>
    <td>
        <props:textProperty name="${commonStrings.conanUserKey}" className="longField" maxlength="256"/>
        <span class="smallNote">This is the "user" in the Conan package reference.</span>
        <span class="error" id="${commonStrings.conanUserKey}_error"></span>
    </td>
</tr>

<%-- Conan channel --%>
<tr>
    <th><label for="${commonStrings.conanChannelKey}">Conan channel: <l:star/></label></th>
    <td>
        <props:textProperty name="${commonStrings.conanChannelKey}" className="longField" maxlength="256"/>
        <span class="smallNote">This is the "channel" in the Conan package reference.</span>
        <span class="error" id="${commonStrings.conanChannelKey}_error"></span>
    </td>
</tr>

<%-- Conan options path --%>
<tr class="advancedSetting">
    <th>
        <label for="${commonStrings.conanOptionsPathKey}">Path to Conan options file
            (<code>${commonStrings.conanDefaultOptionsFileName}</code>):</label>
    </th>
    <td>
        <props:textProperty name="${commonStrings.conanOptionsPathKey}" className="longField" maxlength="512"/>
        <span class="smallNote">Enter path to a Conan options file, relative to working directory or leave blank to use
            the default. Runtime error will be thrown if a value is provided by the file cannot be found.</span>
    </td>
</tr>

<%-- Extra Conan options --%>
<tr class="advancedSetting">
    <th>
        <label for="${commonStrings.conanExtraConanOptionsKey}">Additional Conan settings and options: </label>
    </th>
    <td>
        <c:set var="propName" value="${commonStrings.conanExtraConanOptionsKey}"/>
        <props:textarea name="prop:${propName}" textAreaName="prop:${propName}"
                        value="${propertiesBean.properties[propName]}"
                        linkTitle="Add extra settings and options" cols="70" rows="3"
                        expanded="${not empty propertiesBean.properties[propName]}" className="longField"/>
        <span class="smallNote">Extra Conan settings and options that will be used for each build, even in multi-build
            projects using a <code>conanopts.json</code> file.</span>
    </td>
</tr>

<%-- Docker image --%>
<l:settingsGroup title="Docker Settings">
    <style>
        .smallNote.pullImage code {
            font-size: 12px;
        }
    </style>

    <c:set var="propName" value="${commonStrings.conanDockerImageNameKey}"/>
    <c:set var="dockerWrapperEnabled" value="${not empty propertiesBean.properties[propName]}"/>

    <tr>
        <th><label for="${commonStrings.conanDockerImageNameKey}">Run step within Docker container: </label></th>
        <td>
            <props:textProperty name="${commonStrings.conanDockerImageNameKey}" className="js-docker-property longField"
                                onchange="BS.RLC.onDockerImageUpdate(this.value, 'js-conanDockerExtra')"
                                onkeyup="BS.RLC.onDockerImageUpdate(this.value, 'js-conanDockerExtra')"/>
            <span class="smallNote">E.g. conanio/gcc72. TeamCity will start a container from the specified image and
                will try to run this build step within this container. <bs:help file="Docker Wrapper"/></span>
        </td>
    </tr>

    <tr class="${dockerWrapperEnabled ? '' : 'hidden'} js-conanDockerExtra">
        <th>
            <label for="${commonStrings.conanDockerPlatformKey}">Docker image platform:</label>
        </th>
        <td>
            <props:selectProperty name="${commonStrings.conanDockerPlatformKey}" enableFilter="true"
                                  className="mediumField">
                <props:option value="">&lt;Any&gt;</props:option>
                <props:option value="linux">Linux</props:option>
                <props:option value="windows">Windows</props:option>
            </props:selectProperty>
        </td>
    </tr>

    <tr class="${dockerWrapperEnabled ? '' : 'hidden'} js-conanDockerExtra">
        <th>
            <label for="${commonStrings.conanDockerPullEnabledKey}">Pull image explicitly:</label>
        </th>
        <td>
            <label>
                <props:checkboxProperty name="${commonStrings.conanDockerPullEnabledKey}"/>
                <span class="smallNote pullImage">If enabled, <code>docker pull &lt;imageName&gt;</code> will be run
                    before <code>docker run</code> command.</span>
            </label>
        </td>
    </tr>


    <tr class="${dockerWrapperEnabled ? '' : 'hidden'} js-conanDockerExtra">
        <th>
            <label for="${commonStrings.conanDockerParametersKey}">Additional docker run arguments:</label>
        </th>
        <td>
            <c:set var="propName" value="${commonStrings.conanDockerParametersKey}"/>
            <props:textarea name="prop:${propName}" textAreaName="prop:${propName}"
                            value="${propertiesBean.properties[propName]}"
                            linkTitle="Edit arguments" cols="70" rows="3"
                            expanded="${not empty propertiesBean.properties[propName]}" className="longField"/>
            <span class="smallNote">
                Default argument is <code>--rm</code>, you can specify additional ones.
            </span>
        </td>
    </tr>
</l:settingsGroup>

<script>
    BS.RLC.initPage();
</script>
